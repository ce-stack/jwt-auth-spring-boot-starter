package io.github.cestack.jwtauth.token;

import io.github.cestack.jwtauth.spi.TokenRevocationStore;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryTokenRevocationStore implements TokenRevocationStore {

    private final ConcurrentMap<String, Instant> revokedTokens =
            new ConcurrentHashMap<>();

    @Override
    public void revoke(String tokenId, Instant expiresAt) {
        revokedTokens.put(
                tokenId,
                expiresAt
        );
    }

    @Override
    public boolean isRevoked(String tokenId) {
        Instant expiresAt =
                revokedTokens.get(tokenId);

        if (expiresAt == null) {
            return false;
        }

        if (expiresAt.isBefore(Instant.now())) {

            revokedTokens.remove(tokenId);

            return false;
        }

        return true;
    }
}
