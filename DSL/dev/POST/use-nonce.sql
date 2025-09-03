update request_nonces set used_at = now() where nonce = :updated_nonce;
