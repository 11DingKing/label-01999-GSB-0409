package com.lottery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lottery.common.ResultCode;
import com.lottery.dto.response.LatestWinnerResponse;
import com.lottery.dto.response.PrizeRecordResponse;
import com.lottery.entity.Activity;
import com.lottery.entity.Prize;
import com.lottery.entity.PrizeRecord;
import com.lottery.entity.User;
import com.lottery.exception.BusinessException;
import com.lottery.mapper.ActivityMapper;
import com.lottery.mapper.PrizeMapper;
import com.lottery.mapper.PrizeRecordMapper;
import com.lottery.mapper.UserMapper;
import com.lottery.service.PrizeRecordService;
import com.lottery.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 中奖记录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrizeRecordServiceImpl implements PrizeRecordService {

    private final PrizeRecordMapper prizeRecordMapper;
    private final PrizeMapper prizeMapper;
    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;

    @Override
    public List<PrizeRecordResponse> getMyRecords() {
        Long userId = UserContext.getUserId();
        
        List<PrizeRecord> records = prizeRecordMapper.selectList(
                new LambdaQueryWrapper<PrizeRecord>()
                        .eq(PrizeRecord::getUserId, userId)
                        .isNotNull(PrizeRecord::getPrizeId)
                        .ne(PrizeRecord::getPrizeName, "谢谢参与")
                        .orderByDesc(PrizeRecord::getDrawTime)
        );

        if (records.isEmpty()) {
            return List.of();
        }

        // 批量查询奖品和活动信息
        List<Long> prizeIds = records.stream()
                .map(PrizeRecord::getPrizeId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        List<Long> activityIds = records.stream()
                .map(PrizeRecord::getActivityId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Prize> prizeMap = prizeIds.isEmpty() ? Map.of() : 
                prizeMapper.selectBatchIds(prizeIds).stream()
                        .collect(Collectors.toMap(Prize::getId, p -> p));
        Map<Long, Activity> activityMap = activityIds.isEmpty() ? Map.of() :
                activityMapper.selectBatchIds(activityIds).stream()
                        .collect(Collectors.toMap(Activity::getId, a -> a));

        return records.stream()
                .map(record -> {
                    Prize prize = prizeMap.get(record.getPrizeId());
                    Activity activity = activityMap.get(record.getActivityId());
                    
                    return PrizeRecordResponse.builder()
                            .id(record.getId())
                            .prizeId(record.getPrizeId())
                            .prizeName(record.getPrizeName())
                            .prizeImage(prize != null ? prize.getImage() : null)
                            .activityName(activity != null ? activity.getName() : null)
                            .status(record.getStatus())
                            .statusText(record.getStatus() == 0 ? "待领取" : "已领取")
                            .drawTime(record.getDrawTime())
                            .receiveTime(record.getReceiveTime())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void receivePrize(Long recordId) {
        Long userId = UserContext.getUserId();
        
        PrizeRecord record = prizeRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ResultCode.RECORD_NOT_EXIST);
        }
        
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        
        if (record.getStatus() == 1) {
            throw new BusinessException(ResultCode.RECORD_ALREADY_RECEIVED);
        }

        record.setStatus(1);
        record.setReceiveTime(LocalDateTime.now());
        prizeRecordMapper.updateById(record);
        
        log.info("用户领取奖品: userId={}, recordId={}, prizeName={}", 
                userId, recordId, record.getPrizeName());
    }

    @Override
    public List<LatestWinnerResponse> getLatestWinners() {
        // 查询最近20条中奖记录（排除谢谢参与）
        List<PrizeRecord> records = prizeRecordMapper.selectList(
                new LambdaQueryWrapper<PrizeRecord>()
                        .isNotNull(PrizeRecord::getPrizeId)
                        .ne(PrizeRecord::getPrizeName, "谢谢参与")
                        .orderByDesc(PrizeRecord::getDrawTime)
                        .last("LIMIT 20")
        );

        if (records.isEmpty()) {
            return List.of();
        }

        // 批量查询用户信息
        List<Long> userIds = records.stream()
                .map(PrizeRecord::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return records.stream()
                .map(record -> {
                    User user = userMap.get(record.getUserId());
                    String nickname = user != null ? maskNickname(user.getNickname()) : "***";
                    String avatar = user != null ? user.getAvatar() : null;
                    
                    return LatestWinnerResponse.builder()
                            .nickname(nickname)
                            .avatar(avatar)
                            .prizeName(record.getPrizeName())
                            .drawTime(record.getDrawTime())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 脱敏昵称
     */
    private String maskNickname(String nickname) {
        if (nickname == null || nickname.length() <= 2) {
            return "***";
        }
        return nickname.charAt(0) + "***" + nickname.charAt(nickname.length() - 1);
    }
}
