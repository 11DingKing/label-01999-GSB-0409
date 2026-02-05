<template>
  <div class="activity-page">
    <!-- 顶部导航条 -->
    <div class="top-bar">
      <div class="bar-content">
        <div class="bar-left">
          <el-button text class="back-btn" @click="router.push('/home')">
            <el-icon><ArrowLeft /></el-icon>
            <span>返回活动大厅</span>
          </el-button>
        </div>
        <div class="bar-center" v-if="activity">
          <h1>{{ activity.name }}</h1>
        </div>
        <div class="bar-right">
          <div class="draw-count" v-if="drawCount">
            <el-icon><Ticket /></el-icon>
            <span>今日剩余 <strong>{{ drawCount.dailyRemaining }}</strong> 次</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-wrapper">
      <div class="content-layout">
        <!-- 左侧：抽奖区域 -->
        <div class="lottery-section">
          <!-- 活动描述 -->
          <div class="activity-desc" v-if="activity">
            <p>{{ activity.description }}</p>
            <div class="desc-tags">
              <span class="tag"><el-icon><Clock /></el-icon>每日 {{ activity.dailyLimit }} 次</span>
              <span class="tag"><el-icon><Calendar /></el-icon>{{ formatDate(activity.endTime) }} 截止</span>
            </div>
          </div>

          <!-- 九宫格抽奖 -->
          <div class="lottery-box">
            <div class="lottery-inner">
              <!-- 灯光边框 -->
              <div class="lights">
                <span v-for="i in 28" :key="i" class="bulb" :class="{ on: lightOn === (i % 2 === 0) }"></span>
              </div>
              
              <!-- 九宫格 -->
              <div class="grid-wrapper">
                <div
                  v-for="(item, index) in gridItems"
                  :key="index"
                  class="grid-cell"
                  :class="{
                    'center': index === 4,
                    'active': currentIndex === index && isSpinning,
                    'winner': !isSpinning && winnerIndex === index
                  }"
                  @click="index === 4 && handleDraw()"
                >
                  <template v-if="index === 4">
                    <div class="start-btn" :class="{ spinning: isSpinning, disabled: !canDraw }">
                      <span class="btn-main">{{ isSpinning ? '抽奖中' : '开始' }}</span>
                      <span class="btn-sub">{{ isSpinning ? '...' : (canDraw ? '点击抽奖' : '次数用完') }}</span>
                    </div>
                  </template>
                  <template v-else>
                    <div class="prize-box">
                      <img :src="item.image" :alt="item.name" @error="handleImageError" />
                      <span>{{ item.name }}</span>
                    </div>
                  </template>
                </div>
              </div>
            </div>
          </div>

          <!-- 提示 -->
          <div class="tips">
            <el-icon><InfoFilled /></el-icon>
            <span>中奖后请及时前往"我的奖品"页面领取</span>
          </div>
        </div>

        <!-- 右侧：奖品列表 -->
        <div class="prize-section">
          <div class="prize-card">
            <div class="card-title">
              <el-icon><Present /></el-icon>
              <span>奖品列表</span>
            </div>
            <div class="prize-list">
              <div class="prize-row" v-for="prize in displayPrizes" :key="prize.id">
                <div class="prize-img">
                  <img :src="prize.image" :alt="prize.name" @error="handleImageError" />
                </div>
                <div class="prize-info">
                  <span class="name">{{ prize.name }}</span>
                  <span class="stock" :class="{ low: prize.remainingCount < 10 }">库存: {{ prize.remainingCount }}</span>
                </div>
              </div>
              <div class="prize-row empty" v-if="displayPrizes.length === 0">
                <span>暂无奖品</span>
              </div>
            </div>
          </div>

          <el-button type="primary" size="large" class="my-prize-btn" @click="router.push('/records')">
            <el-icon><Trophy /></el-icon>
            我的奖品
          </el-button>
        </div>
      </div>
    </div>

    <!-- 中奖弹窗 -->
    <el-dialog v-model="showResultDialog" width="420px" :show-close="false" :close-on-click-modal="false" center class="result-dialog">
      <div class="result-box">
        <div class="result-icon" :class="{ won: drawResult?.won }">
          <img v-if="drawResult?.won" :src="drawResult.prizeImage" :alt="drawResult.prizeName" @error="handleImageError" />
          <span v-else class="emoji">🍀</span>
        </div>
        <h2>{{ drawResult?.won ? '🎉 恭喜中奖！' : '继续加油！' }}</h2>
        <p class="msg">{{ drawResult?.won ? `您获得了「${drawResult.prizeName}」` : '谢谢参与，下次一定好运！' }}</p>
        <p class="hint" v-if="drawResult?.won">请前往"我的奖品"页面领取</p>
      </div>
      <template #footer>
        <div class="result-btns">
          <el-button size="large" @click="handleCloseResult">{{ drawResult?.won ? '继续抽奖' : '再试一次' }}</el-button>
          <el-button v-if="drawResult?.won" type="primary" size="large" @click="goToRecords">立即领取</el-button>
        </div>
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
const gridOrder = [0, 1, 2, 5, 8, 7, 6, 3]
const defaultImage = 'https://img.icons8.com/color/200/gift.png'

const canDraw = computed(() => drawCount.value && drawCount.value.dailyRemaining > 0)

const gridItems = computed(() => {
  const items = []
  const prizeList = [...prizes.value]
  const thanksPrize = prizeList.find(p => p.name === '谢谢参与') || { id: 0, name: '谢谢参与', image: 'https://img.icons8.com/fluency/200/good-luck.png' }
  const realPrizes = prizeList.filter(p => p.name !== '谢谢参与')
  const thanksPositions = [2, 5, 7]
  const prizePositions = [0, 1, 3, 4, 6]
  const gridContent = new Array(8).fill(null)
  
  thanksPositions.forEach((pos, idx) => { gridContent[pos] = { ...thanksPrize, id: `thanks-${idx}` } })
  let prizeIdx = 0
  prizePositions.forEach(pos => {
    if (prizeIdx < realPrizes.length) { gridContent[pos] = realPrizes[prizeIdx]; prizeIdx++ }
    else { gridContent[pos] = { ...thanksPrize, id: `thanks-extra-${pos}` } }
  })
  
  for (let i = 0; i < 9; i++) {
    if (i === 4) { items.push({ isCenter: true }) }
    else { items.push(gridContent[gridOrder.indexOf(i)]) }
  }
  return items
})

const displayPrizes = computed(() => prizes.value.filter(p => p.name !== '谢谢参与'))

onMounted(() => {
  fetchActivityDetail()
  fetchPrizes()
  fetchDrawCount()
  lightTimer = setInterval(() => { lightOn.value = !lightOn.value }, 500)
})

onUnmounted(() => { if (lightTimer) clearInterval(lightTimer) })

async function fetchActivityDetail() {
  try { const res = await getActivityDetail(activityId.value); activity.value = res.data }
  catch (error) { ElMessage.error('获取活动信息失败') }
}

async function fetchPrizes() {
  try { const res = await getActivityPrizes(activityId.value); prizes.value = res.data }
  catch (error) { ElMessage.error('获取奖品列表失败') }
}

async function fetchDrawCount() {
  try { const res = await getRemainingDrawCount(activityId.value); drawCount.value = res.data }
  catch (error) { console.error('获取抽奖次数失败:', error) }
}

async function handleDraw() {
  if (isSpinning.value) return
  if (!canDraw.value) { ElMessage.warning('今日抽奖次数已用完，明天再来吧！'); return }

  isSpinning.value = true
  winnerIndex.value = -1
  
  try {
    const res = await draw(activityId.value)
    drawResult.value = res.data

    let targetGridIndex = -1
    if (res.data.prizeId) {
      for (let i = 0; i < 9; i++) {
        if (i !== 4 && gridItems.value[i].id === res.data.prizeId) { targetGridIndex = i; break }
      }
    }
    if (targetGridIndex === -1) {
      const thanksIndexes = []
      for (let i = 0; i < 9; i++) { if (i !== 4 && gridItems.value[i].name === '谢谢参与') { thanksIndexes.push(i) } }
      targetGridIndex = thanksIndexes.length > 0 ? thanksIndexes[Math.floor(Math.random() * thanksIndexes.length)] : 0
    }

    await runAnimation(targetGridIndex)
    winnerIndex.value = targetGridIndex
    isSpinning.value = false
    setTimeout(() => { showResultDialog.value = true; fetchDrawCount(); fetchPrizes() }, 500)
  } catch (error) { isSpinning.value = false; currentIndex.value = -1; ElMessage.error('抽奖失败，请重试') }
}

function runAnimation(targetIndex) {
  return new Promise((resolve) => {
    const targetOrderIndex = gridOrder.indexOf(targetIndex)
    const totalSteps = 3 * 8 + targetOrderIndex
    let step = 0, speed = 60

    function animate() {
      currentIndex.value = gridOrder[step % 8]
      step++
      if (step > totalSteps) { currentIndex.value = targetIndex; resolve(); return }
      const remaining = totalSteps - step
      if (remaining <= 16) {
        if (remaining <= 4) speed = 280 + (4 - remaining) * 80
        else if (remaining <= 8) speed = 140 + (8 - remaining) * 35
        else speed = 80 + (16 - remaining) * 8
      }
      setTimeout(animate, speed)
    }
    animate()
  })
}

function handleCloseResult() { showResultDialog.value = false; winnerIndex.value = -1 }
function goToRecords() { showResultDialog.value = false; router.push('/records') }
function handleImageError(e) { e.target.src = defaultImage }
function formatDate(dateStr) {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
</script>

<style lang="scss" scoped>
.activity-page {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}

/* 顶部导航条 */
.top-bar {
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  position: sticky;
  top: 0;
  z-index: 100;
}

.bar-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 32px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.bar-left {
  flex: 1;
  
  .back-btn {
    color: #64748b;
    font-size: 14px;
    padding: 8px 16px;
    border-radius: 8px;
    transition: all 0.2s;
    
    &:hover {
      color: #6366f1;
      background: #f1f5f9;
    }
    
    .el-icon { margin-right: 6px; }
  }
}

.bar-center {
  flex: 2;
  text-align: center;
  
  h1 {
    font-size: 18px;
    font-weight: 600;
    color: #1e293b;
    margin: 0;
  }
}

.bar-right {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  
  .draw-count {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 20px;
    background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
    border-radius: 20px;
    color: #fff;
    font-size: 14px;
    
    strong { font-size: 18px; margin: 0 2px; }
  }
}

/* 主内容区 */
.main-wrapper {
  flex: 1;
  padding: 32px;
}

.content-layout {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 32px;
  align-items: start;
}

/* 左侧抽奖区 */
.lottery-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.activity-desc {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  
  p {
    font-size: 15px;
    color: #475569;
    line-height: 1.7;
    margin: 0 0 16px 0;
  }
  
  .desc-tags {
    display: flex;
    gap: 12px;
    
    .tag {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: #64748b;
      padding: 8px 14px;
      background: #f8fafc;
      border-radius: 8px;
      
      .el-icon { color: #94a3b8; }
    }
  }
}

/* 抽奖盒子 */
.lottery-box {
  background: #fff;
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  display: flex;
  justify-content: center;
}

.lottery-inner {
  position: relative;
  padding: 20px;
  background: linear-gradient(145deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 20px;
}

/* 灯光 */
.lights {
  position: absolute;
  inset: 6px;
  border-radius: 16px;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-around;
  align-content: space-around;
  pointer-events: none;
  
  .bulb {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    transition: all 0.3s;
    
    &.on {
      background: #fbbf24;
      box-shadow: 0 0 8px #fbbf24;
    }
    
    &:nth-child(odd).on {
      background: #f87171;
      box-shadow: 0 0 8px #f87171;
    }
  }
}

/* 九宫格 */
.grid-wrapper {
  display: grid;
  grid-template-columns: repeat(3, 120px);
  grid-template-rows: repeat(3, 120px);
  gap: 8px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 14px;
  position: relative;
  z-index: 1;
}

.grid-cell {
  width: 120px;
  height: 120px;
  border-radius: 12px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: all 0.15s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: 12px;
    border: 3px solid transparent;
    transition: all 0.15s;
  }
  
  &.active {
    transform: scale(1.03);
    &::after { border-color: #fbbf24; box-shadow: inset 0 0 16px rgba(251, 191, 36, 0.25); }
    .prize-box { background: linear-gradient(145deg, #fffbeb 0%, #fef3c7 100%); }
  }
  
  &.winner {
    animation: glow 0.5s ease-in-out infinite alternate;
    &::after { border-color: #ef4444; }
  }
  
  &.center { background: transparent; box-shadow: none; }
}

@keyframes glow {
  from { box-shadow: 0 0 12px rgba(239, 68, 68, 0.4); }
  to { box-shadow: 0 0 24px rgba(239, 68, 68, 0.7); }
}

.prize-box {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px;
  border-radius: 12px;
  
  img { width: 52px; height: 52px; object-fit: contain; margin-bottom: 6px; }
  span { font-size: 11px; font-weight: 600; color: #374151; text-align: center; max-width: 90px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
}

/* 开始按钮 */
.start-btn {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(145deg, #fbbf24 0%, #f59e0b 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 6px 20px rgba(251, 191, 36, 0.4);
  transition: all 0.3s;
  
  .btn-main { font-size: 20px; font-weight: 700; color: #7c2d12; }
  .btn-sub { font-size: 11px; color: #92400e; margin-top: 2px; }
  
  &:hover:not(.disabled):not(.spinning) {
    transform: scale(1.05);
    box-shadow: 0 8px 28px rgba(251, 191, 36, 0.5);
  }
  
  &:active:not(.disabled):not(.spinning) { transform: scale(0.98); }
  
  &.spinning { animation: pulse 1s ease-in-out infinite; cursor: not-allowed; }
  
  &.disabled {
    background: linear-gradient(145deg, #9ca3af 0%, #6b7280 100%);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    cursor: not-allowed;
  }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.02); }
}

.tips {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px;
  background: #fff;
  border-radius: 10px;
  color: #64748b;
  font-size: 13px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  
  .el-icon { color: #6366f1; font-size: 16px; }
}

/* 右侧奖品区 */
.prize-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.prize-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  
  .card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 16px 20px;
    background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
    color: #fff;
    font-size: 15px;
    font-weight: 600;
    
    .el-icon { font-size: 18px; }
  }
}

.prize-list {
  padding: 12px;
  max-height: 400px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.prize-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 10px;
  transition: all 0.2s;
  
  &:hover { background: #f1f5f9; transform: translateX(4px); }
  
  &.empty { justify-content: center; color: #94a3b8; font-size: 14px; padding: 24px; }
  
  .prize-img {
    width: 44px;
    height: 44px;
    flex-shrink: 0;
    background: #fff;
    border-radius: 8px;
    padding: 4px;
    
    img { width: 100%; height: 100%; object-fit: contain; }
  }
  
  .prize-info {
    flex: 1;
    min-width: 0;
    
    .name { display: block; font-size: 13px; font-weight: 600; color: #1e293b; margin-bottom: 2px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .stock { font-size: 12px; color: #10b981; &.low { color: #ef4444; } }
  }
}

.my-prize-btn {
  height: 48px;
  font-size: 15px;
  border-radius: 12px;
}

/* 中奖弹窗 */
.result-dialog {
  :deep(.el-dialog) { border-radius: 20px; }
  :deep(.el-dialog__header) { display: none; }
  :deep(.el-dialog__body) { padding: 40px 32px 24px; }
  :deep(.el-dialog__footer) { padding: 0 32px 32px; }
}

.result-box {
  text-align: center;
  
  .result-icon {
    width: 100px;
    height: 100px;
    margin: 0 auto 20px;
    background: #f1f5f9;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &.won { background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%); }
    img { width: 70px; height: 70px; object-fit: contain; }
    .emoji { font-size: 50px; }
  }
  
  h2 { font-size: 22px; font-weight: 700; color: #1e293b; margin-bottom: 10px; }
  .msg { font-size: 15px; color: #475569; margin-bottom: 6px; }
  .hint { font-size: 13px; color: #94a3b8; }
}

.result-btns {
  display: flex;
  gap: 12px;
  .el-button { flex: 1; height: 48px; font-size: 15px; border-radius: 10px; }
}

/* 响应式 */
@media (max-width: 1024px) {
  .content-layout { grid-template-columns: 1fr; }
  .prize-section { order: -1; }
  .prize-list { max-height: 180px; flex-direction: row; flex-wrap: wrap; }
  .prize-row { width: calc(50% - 4px); }
}

@media (max-width: 768px) {
  .main-wrapper { padding: 16px; }
  .bar-content { padding: 0 16px; }
  .bar-center { display: none; }
  .grid-wrapper { grid-template-columns: repeat(3, 100px); grid-template-rows: repeat(3, 100px); }
  .grid-cell { width: 100px; height: 100px; }
  .start-btn { width: 100px; height: 100px; .btn-main { font-size: 16px; } }
  .prize-box img { width: 40px; height: 40px; }
}
</style>
