package com.moabood.financetracker.goal;

import com.moabood.financetracker.common.ResourceNotFoundException;
import com.moabood.financetracker.user.User;
import com.moabood.financetracker.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserService userService;

    public List<GoalDto> getAll() {
        User user = userService.getCurrentUser();
        return goalRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public GoalDto create(CreateGoalRequest request) {
        User user = userService.getCurrentUser();
        Goal goal = new Goal();
        goal.setUser(user);
        applyRequest(goal, request);
        return toDto(goalRepository.save(goal));
    }

    @Transactional
    public GoalDto update(Long id, CreateGoalRequest request) {
        User user = userService.getCurrentUser();
        Goal goal = goalRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));
        applyRequest(goal, request);
        return toDto(goalRepository.save(goal));
    }

    @Transactional
    public void delete(Long id) {
        User user = userService.getCurrentUser();
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));
        if (!goal.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied");
        }
        goalRepository.delete(goal);
    }

    private void applyRequest(Goal goal, CreateGoalRequest request) {
        goal.setName(request.getName());
        goal.setDescription(request.getDescription());
        goal.setTargetAmount(request.getTargetAmount());
        if (request.getCurrentAmount() != null) {
            goal.setCurrentAmount(request.getCurrentAmount());
        }
        goal.setTargetDate(request.getTargetDate());
        if (request.getIcon() != null && !request.getIcon().isBlank()) {
            goal.setIcon(request.getIcon());
        }
        if (request.getColor() != null && !request.getColor().isBlank()) {
            goal.setColor(request.getColor());
        }
        if (request.getStatus() != null) {
            goal.setStatus(request.getStatus());
        }
        // Auto-mark as achieved when current reaches target
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0
                && goal.getStatus() == GoalStatus.IN_PROGRESS) {
            goal.setStatus(GoalStatus.ACHIEVED);
        }
    }

    GoalDto toDto(Goal goal) {
        BigDecimal target = goal.getTargetAmount();
        BigDecimal current = goal.getCurrentAmount();
        BigDecimal remaining = target.subtract(current).max(BigDecimal.ZERO);
        double percent = target.compareTo(BigDecimal.ZERO) == 0 ? 0.0
                : current.divide(target, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;

        return GoalDto.builder()
                .id(goal.getId())
                .name(goal.getName())
                .description(goal.getDescription())
                .targetAmount(target)
                .currentAmount(current)
                .percentComplete(Math.min(percent, 100.0))
                .amountRemaining(remaining)
                .targetDate(goal.getTargetDate())
                .status(goal.getStatus())
                .icon(goal.getIcon())
                .color(goal.getColor())
                .createdAt(goal.getCreatedAt())
                .build();
    }
}
