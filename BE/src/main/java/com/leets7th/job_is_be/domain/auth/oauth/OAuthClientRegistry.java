package com.leets7th.job_is_be.domain.auth.oauth;

import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class OAuthClientRegistry {

    private final Map<SocialType, SocialOAuthClient> clients;

    public OAuthClientRegistry(List<SocialOAuthClient> clients) {
        Map<SocialType, SocialOAuthClient> clientMap = new EnumMap<>(SocialType.class);
        clients.forEach(client -> clientMap.put(client.supports(), client));
        this.clients = Map.copyOf(clientMap);
    }

    public SocialType parseProvider(String provider) {
        try {
            return SocialType.valueOf(provider.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new GeneralException(ErrorStatus.OAUTH_PROVIDER_NOT_SUPPORTED);
        }
    }

    public SocialOAuthClient get(SocialType socialType) {
        SocialOAuthClient client = clients.get(socialType);
        if (client == null) {
            throw new GeneralException(ErrorStatus.OAUTH_PROVIDER_NOT_SUPPORTED);
        }
        return client;
    }
}
