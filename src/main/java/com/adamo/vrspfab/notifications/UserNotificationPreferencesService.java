package com.adamo.vrspfab.notifications;


import com.adamo.vrspfab.common.SecurityUtilsService;
import com.adamo.vrspfab.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserNotificationPreferencesService {

    private final UserNotificationPreferencesRepository preferencesRepository;
    private final UserNotificationPreferencesMapper preferencesMapper;
    private final SecurityUtilsService securityUtilsService;

    @Transactional(readOnly = true)
    public UserNotificationPreferencesDto getPreferencesForCurrentUser() {
        User user = securityUtilsService.getCurrentAuthenticatedUser();
        UserNotificationPreferences preferences = getOrCreatePreferences(user);
        return preferencesMapper.toDto(preferences);
    }

    @Transactional
    public UserNotificationPreferencesDto updatePreferencesForCurrentUser(UserNotificationPreferencesDto dto) {
        User user = securityUtilsService.getCurrentAuthenticatedUser();
        UserNotificationPreferences preferences = getOrCreatePreferences(user);
        preferencesMapper.updateFromDto(dto, preferences);
        return preferencesMapper.toDto(preferencesRepository.save(preferences));
    }

    private UserNotificationPreferences getOrCreatePreferences(User user) {
        return preferencesRepository.findById(user.getId()).orElseGet(() -> {
            UserNotificationPreferences newPrefs = new UserNotificationPreferences();
            newPrefs.setUser(user);
            return preferencesRepository.save(newPrefs);
        });
    }
}