<template>
  <div class="activity-page">
    <div class="page-container">
      <!-- 返回按钮 -->
      <div class="back-btn" @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </div>

      <!-- 活动信息卡片 -->
      <div class="activity-card" v-if="activity">
        <h1 class="activity-title">{{ activity.name }}</h1>
        <p class="activity-desc">{{ activity.description }}</p>
        <div class="draw-count" v-if="drawCount">
          今日剩余 <span class="count">{{ drawCount.dailyRemaining }}</span> 次抽奖机会
        </div>
      </div>

      <!-- 九宫格抽奖区域 -->
      <div class="lottery-section">
        <div class="lottery-wrapper">
          <!-- 装饰边框 -->
          <div class="lottery-border">
            <span v-for="i in 24" :key="i" class="border-light" :class="{ on: lightOn === (i % 2 === 0) }"></span>
          </div>
          
          <!-- 九宫格 -->
          <div class="lottery-grid">
            <div
              v-for="(item, index) in gridItems"
              :key="index"
              class="grid-cell"
              :class="{
                'is-center': index === 4,
                'is-active': currentIndex === index && isSpinning,
                'is-winner': !isSpinning && winnerIndex === index
              }"
              @click="index === 4 && handleDraw()"
            >
              <!-- 中间按钮 -->
              <template v-if="index === 4">
                <div class="start-btn" :class="{ spinning: isSpinning }">
                  <div class="btn-content">
                    <span class="btn-text">{{ isSpinning ? '抽奖中' : '开始' }}</span>
                    <span class="btn-sub" v-if="!isSpinning">点击抽奖</span>
                  </div>
                </div>
              </template>
              <!-- 奖品格子 -->
              <template v-else>
                <div class="prize-cell">
                  <div class="prize-icon">
                    <img 
                      :src="item.image" 
                      :alt="item.name"
                      @error="handleImageError"
                    />
                  </div>
                  <span class="prize-name">{{ item.name }}</span>
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>

      <!-- 奖品列表区域 -->
      <div class="prize-section">
        <div class="section-header">
          <el-icon class="section-icon"><Present /></el-icon>
          <span class="section-title">奖品列表</span>
        </div>
        <div class="prize-grid">
          <div class="prize-item" v-for="prize in displayPrizes" :key="prize.id">
            <div class="prize-image-wrapper">
              <img 
                :src="prize.image" 
                :alt="prize.name"
                @error="handleImageError"
              />
            </div>
            <div class="prize-detail">
              <span class="prize-title">{{ prize.name }}</span>
              <span class="prize-stock" v-if="prize.name !== '谢谢参与'">剩余 {{ prize.remainingCount }} 件</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 中奖弹窗 -->
    <el-dialog
      v-model="showResultDialog"
      :title="drawResult?.won ? '🎉 恭喜中奖' : '😢 很遗憾'"
      width="320px"
      center
      :close-on-click-modal="false"
      class="result-dialog"
    >
      <div class="result-content">
        <div class="result-icon">
          <img
            v-if="drawResult?.won"
            :src="drawResult.prizeImage"
            :alt="drawResult.prizeName"
            @error="handleImageError"
          />
          <span v-else class="emoji">🍀</span>
        </div>
        <p class="result-text">
          {{ drawResult?.won ? `恭喜获得 ${drawResult.prizeName}` : '谢谢参与，下次好运！' }}
        </p>
      </div>
      <template #footer>
        <el-button 
          type="primary" 
          size="large"
          class="result-btn"
          @click="handleCloseResult"
        >
          {{ drawResult?.won ? '去领取' : '继续抽奖' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getActivityDetail, getActivityPrizes } from '@/api/activity'
import { draw, getRemainingDrawCount } from '@/api/draw'

const router = useRouter()
const route = useRoute()

const activityId = computed(() => route.params.id)

const activity = ref(null)
const prizes = ref([])
const drawCount = ref(null)
const isSpinning = ref(false)
const currentIndex = ref(-1)
const winnerIndex = ref(-1)
const showResultDialog = ref(false)
const drawResult = ref(null)
const lightOn = ref(true)

let lightTimer = null

// 九宫格顺序：顺时针 0-1-2-5-8-7-6-3
const gridOrder = [0, 1, 2, 5, 8, 7, 6, 3]

// 默认占位图
const defaultImage = 'https://img.icons8.com/color/200/gift.png'

// 构建九宫格数据（谢谢参与均匀分布）
const gridItems = computed(() => {
  const items = []
  const prizeList = [...prizes.value]
  
  const thanksPrize = prizeList.find(p => p.name === '谢谢参与') || {
    id: 0,
    name: '谢谢参与',
    image: 'https://img.icons8.com/fluency/200/good-luck.png'
  }
  
  // 获取真实奖品（不含谢谢参与）
  const realPrizes = prizeList.filter(p => p.name !== '谢谢参与')
  
  // 九宫格8个位置的布局（顺时针：0,1,2,5,8,7,6,3）
  // 让谢谢参与分布在位置 2, 5, 7（均匀间隔）
  // 真实奖品分布在位置 0, 1, 3, 6, 8
  const thanksPositions = [2, 5, 7]  // 谢谢参与的顺时针位置索引
  const prizePositions = [0, 1, 3, 4, 6]  // 真实奖品的顺时针位置索引
  
  // 构建顺时针顺序的8个格子内容
  const gridContent = new Array(8).fill(null)
  
  // 放置谢谢参与
  thanksPositions.forEach((pos, idx) => {
    gridContent[pos] = { ...thanksPrize, id: `thanks-${idx}` }
  })
  
  // 放置真实奖品
  let prizeIdx = 0
  prizePositions.forEach(pos => {
    if (prizeIdx < realPrizes.length) {
      gridContent[pos] = realPrizes[prizeIdx]
      prizeIdx++
    } else {
      gridContent[pos] = { ...thanksPrize, id: `thanks-extra-${pos}` }
    }
  })
  
  // 按九宫格实际位置填充（gridOrder映射顺时针到实际位置）
  for (let i = 0; i < 9; i++) {
    if (i === 4) {
      items.push({ isCenter: true })
    } else {
      const orderIndex = gridOrder.indexOf(i)
      items.push(gridContent[orderIndex])
    }
  }
  
  return items
})

// 奖品列表显示（不显示"谢谢参与"）
const displayPrizes = computed(() => {
  return prizes.value.filter(p => p.name !== '谢谢参与')
})

onMounted(() => {
  fetchActivityDetail()
  fetchPrizes()
  fetchDrawCount()
  
  lightTimer = setInterval(() => {
    lightOn.value = !lightOn.value
  }, 600)
})

onUnmounted(() => {
  if (lightTimer) clearInterval(lightTimer)
})

async function fetchActivityDetail() {
  try {
    const res = await getActivityDetail(activityId.value)
    activity.value = res.data
  } catch (error) {
    ElMessage.error('获取活动信息失败')
  }
}

async function fetchPrizes() {
  try {
    const res = await getActivityPrizes(activityId.value)
    prizes.value = res.data
  } catch (error) {
    ElMessage.error('获取奖品列表失败')
  }
}

async function fetchDrawCount() {
  try {
    const res = await getRemainingDrawCount(activityId.value)
    drawCount.value = res.data
  } catch (error) {
    console.error('获取抽奖次数失败:', error)
  }
}

async function handleDraw() {
  if (isSpinning.value) return
  
  if (drawCount.value && drawCount.value.dailyRemaining <= 0) {
    ElMessage.warning('今日抽奖次数已用完')
    return
  }

  isSpinning.value = true
  winnerIndex.value = -1
  
  try {
    const res = await draw(activityId.value)
    drawResult.value = res.data

    let targetGridIndex = -1
    if (res.data.prizeId) {
      for (let i = 0; i < 9; i++) {
        if (i !== 4 && gridItems.value[i].id === res.data.prizeId) {
          targetGridIndex = i
          break
        }
      }
    }
    
    if (targetGridIndex === -1) {
      const thanksIndexes = []
      for (let i = 0; i < 9; i++) {
        if (i !== 4 && gridItems.value[i].name === '谢谢参与') {
          thanksIndexes.push(i)
        }
      }
      targetGridIndex = thanksIndexes.length > 0 
        ? thanksIndexes[Math.floor(Math.random() * thanksIndexes.length)]
        : 0
    }

    await runAnimation(targetGridIndex)
    
    winnerIndex.value = targetGridIndex
    isSpinning.value = false
    
    setTimeout(() => {
      showResultDialog.value = true
      fetchDrawCount()
      fetchPrizes()
    }, 400)
    
  } catch (error) {
    isSpinning.value = false
    currentIndex.value = -1
    ElMessage.error('抽奖失败，请重试')
  }
}

function runAnimation(targetIndex) {
  return new Promise((resolve) => {
    const targetOrderIndex = gridOrder.indexOf(targetIndex)
    // 至少转3圈，然后停在目标位置
    const minRounds = 3
    const totalSteps = minRounds * 8 + targetOrderIndex
    let step = 0
    let speed = 50  // 初始速度（毫秒）
    
    function animate() {
      const orderIndex = step % 8
      currentIndex.value = gridOrder[orderIndex]
      step++
      
      // 到达目标位置
      if (step > totalSteps) {
        currentIndex.value = targetIndex
        resolve()
        return
      }
      
      // 计算剩余步数
      const remaining = totalSteps - step
      
      // 平滑减速：根据剩余步数逐渐增加间隔
      if (remaining <= 16) {
        // 最后16步开始减速
        if (remaining <= 4) {
          // 最后4步，非常慢
          speed = 300 + (4 - remaining) * 100
        } else if (remaining <= 8) {
          // 倒数5-8步，较慢
          speed = 150 + (8 - remaining) * 40
        } else {
          // 倒数9-16步，开始减速
          speed = 80 + (16 - remaining) * 10
        }
      }
      
      setTimeout(animate, speed)
    }
    
    animate()
  })
}

function handleCloseResult() {
  showResultDialog.value = false
  winnerIndex.value = -1
  
  if (drawResult.value?.won) {
    router.push('/records')
  }
}

function handleImageError(e) {
  e.target.src = defaultImage
}
</script>

<style lang="scss" scoped>
.activity-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.page-container {
  padding: 16px;
  padding-bottom: 32px;
  max-width: 480px;
  margin: 0 auto;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  cursor: pointer;
  padding: 8px 0;
  transition: opacity 0.2s;
  
  &:hover {
    opacity: 0.8;
  }
}

/* 活动信息卡片 */
.activity-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 24px;
  margin: 16px 0 24px;
  text-align: center;
  border: 1px solid rgba(255, 255, 255, 0.2);

  .activity-title {
    font-size: 24px;
    font-weight: 700;
    color: #fff;
    margin-bottom: 8px;
  }

  .activity-desc {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.8);
    margin-bottom: 16px;
    line-height: 1.5;
  }

  .draw-count {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    background: rgba(255, 255, 255, 0.2);
    padding: 8px 20px;
    border-radius: 20px;
    color: #fff;
    font-size: 14px;

    .count {
      font-size: 22px;
      font-weight: 700;
      color: #FFE066;
      margin: 0 2px;
    }
  }
}

/* 九宫格抽奖区域 */
.lottery-section {
  display: flex;
  justify-content: center;
  margin-bottom: 32px;
}

.lottery-wrapper {
  position: relative;
  padding: 16px;
}

.lottery-border {
  position: absolute;
  inset: 0;
  border-radius: 20px;
  background: linear-gradient(145deg, #a78bfa 0%, #8b5cf6 100%);
  display: flex;
  flex-wrap: wrap;
  justify-content: space-around;
  align-content: space-around;
  padding: 4px;
  z-index: 0;
  box-shadow: 0 4px 20px rgba(139, 92, 246, 0.3);

  .border-light {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.3);
    transition: all 0.3s;

    &.on {
      background: #FFD700;
      box-shadow: 0 0 8px #FFD700, 0 0 16px rgba(255, 215, 0, 0.5);
    }

    &:nth-child(odd).on {
      background: #FF6B6B;
      box-shadow: 0 0 8px #FF6B6B, 0 0 16px rgba(255, 107, 107, 0.5);
    }
  }
}

.lottery-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(3, 96px);
  grid-template-rows: repeat(3, 96px);
  gap: 8px;
  padding: 8px;
  background: linear-gradient(145deg, #c4b5fd 0%, #a78bfa 100%);
  border-radius: 16px;
}

.grid-cell {
  width: 96px;
  height: 96px;
  border-radius: 12px;
  background: linear-gradient(145deg, #fff 0%, #f5f5f5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: all 0.15s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: 12px;
    border: 3px solid transparent;
    transition: all 0.15s ease;
    pointer-events: none;
  }

  &.is-active {
    transform: scale(1.02);
    
    &::after {
      border-color: #FFD700;
      box-shadow: inset 0 0 20px rgba(255, 215, 0, 0.3);
    }
    
    .prize-cell {
      background: linear-gradient(145deg, #fffbeb 0%, #fef3c7 100%);
    }
  }

  &.is-winner {
    animation: winner-glow 0.6s ease-in-out infinite alternate;
    
    &::after {
      border-color: #FF6B6B;
    }
  }

  &.is-center {
    background: linear-gradient(145deg, #FFD700 0%, #F59E0B 100%);
    cursor: pointer;
    box-shadow: 0 4px 16px rgba(245, 158, 11, 0.4);

    &:hover {
      transform: scale(1.03);
      box-shadow: 0 6px 20px rgba(245, 158, 11, 0.5);
    }

    &:active {
      transform: scale(0.98);
    }
  }
}

@keyframes winner-glow {
  from {
    box-shadow: 0 0 10px rgba(255, 107, 107, 0.5);
  }
  to {
    box-shadow: 0 0 25px rgba(255, 107, 107, 0.8);
  }
}

.prize-cell {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8px;
  border-radius: 12px;
  transition: background 0.15s;

  .prize-icon {
    width: 48px;
    height: 48px;
    margin-bottom: 6px;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: contain;
    }
  }

  .prize-name {
    font-size: 11px;
    font-weight: 600;
    color: #374151;
    text-align: center;
    line-height: 1.3;
    max-width: 80px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.start-btn {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;

  .btn-content {
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .btn-text {
    font-size: 20px;
    font-weight: 700;
    color: #7C2D12;
  }

  .btn-sub {
    font-size: 11px;
    color: #92400E;
    margin-top: 2px;
  }

  &.spinning {
    .btn-text {
      animation: pulse-text 0.8s ease-in-out infinite;
    }
  }
}

@keyframes pulse-text {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

/* 奖品列表区域 */
.prize-section {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.15);

  .section-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 16px;

    .section-icon {
      font-size: 20px;
      color: #FFE066;
    }

    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: #fff;
    }
  }
}

.prize-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.prize-item {
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  }

  .prize-image-wrapper {
    width: 48px;
    height: 48px;
    margin: 0 auto 8px;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: contain;
    }
  }

  .prize-detail {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .prize-title {
      font-size: 12px;
      font-weight: 600;
      color: #374151;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .prize-stock {
      font-size: 11px;
      color: #9CA3AF;
    }
  }
}

/* 中奖弹窗 */
.result-dialog {
  :deep(.el-dialog) {
    border-radius: 16px;
  }
  
  :deep(.el-dialog__header) {
    padding-top: 24px;
  }
  
  :deep(.el-dialog__title) {
    font-size: 18px;
    font-weight: 600;
  }
}

.result-content {
  text-align: center;
  padding: 16px 0 24px;

  .result-icon {
    width: 100px;
    height: 100px;
    margin: 0 auto 16px;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: contain;
    }
    
    .emoji {
      font-size: 72px;
      line-height: 100px;
    }
  }

  .result-text {
    font-size: 16px;
    font-weight: 600;
    color: #374151;
  }
}

.result-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  border-radius: 8px;
}
</style>
