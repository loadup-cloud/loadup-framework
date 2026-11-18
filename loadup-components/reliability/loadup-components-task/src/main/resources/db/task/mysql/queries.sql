-- name: UPSERT
INSERT INTO {{prefix}}task(id, created_at, updated_at, task_type, biz_id, payload, trace_id,
    status, next_execute_at, max_attempts)
VALUES (?, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), ?, ?, ?, ?, 'PENDING',
    COALESCE(?, CURRENT_TIMESTAMP(6)), ?)
ON DUPLICATE KEY UPDATE id = id

-- name: LOCK_BY_BUSINESS
SELECT * FROM {{prefix}}task WHERE task_type = ? AND biz_id = ? FOR UPDATE

-- name: RECOVER_EXPIRED
UPDATE {{prefix}}task
SET status = 'FAILED', claim_token = NULL, lease_until = NULL, last_error = 'LeaseExpired', updated_at = CURRENT_TIMESTAMP(6)
WHERE status = 'RUNNING' AND lease_until <= CURRENT_TIMESTAMP(6) AND attempts >= max_attempts
  AND (? IS NULL OR task_type = ?)

-- name: CLAIM_CANDIDATE
SELECT * FROM {{prefix}}task
WHERE (? IS NULL OR task_type = ?) AND attempts < max_attempts
  AND ((status = 'PENDING' AND next_execute_at <= CURRENT_TIMESTAMP(6))
    OR (status = 'RUNNING' AND lease_until <= CURRENT_TIMESTAMP(6)))
ORDER BY next_execute_at, id LIMIT 1 FOR UPDATE SKIP LOCKED

-- name: CLAIM
UPDATE {{prefix}}task
SET status = 'RUNNING', attempts = attempts + 1, claim_token = ?,
    lease_until = TIMESTAMPADD(MICROSECOND, ? * 1000, CURRENT_TIMESTAMP(6)), updated_at = CURRENT_TIMESTAMP(6)
WHERE id = ?

-- name: LOCK_OWNER
SELECT id FROM {{prefix}}task
WHERE id = ? AND status = 'RUNNING' AND claim_token = ? FOR UPDATE

-- name: COMPLETE
DELETE FROM {{prefix}}task
WHERE id = ? AND status = 'RUNNING' AND claim_token = ? AND lease_until > CURRENT_TIMESTAMP(6)

-- name: FAIL
UPDATE {{prefix}}task
SET status = ?, claim_token = NULL, lease_until = NULL, last_error = ?,
    next_execute_at = COALESCE(TIMESTAMPADD(MICROSECOND, ? * 1000, CURRENT_TIMESTAMP(6)), next_execute_at), updated_at = CURRENT_TIMESTAMP(6)
WHERE id = ? AND status = 'RUNNING' AND claim_token = ? AND lease_until > CURRENT_TIMESTAMP(6)

-- name: FIND_BY_ID
SELECT * FROM {{prefix}}task WHERE id = ?

-- name: FIND_BY_BUSINESS
SELECT * FROM {{prefix}}task WHERE task_type = ? AND biz_id = ?

-- name: FIND_FAILURES
SELECT * FROM {{prefix}}task WHERE status = 'FAILED' ORDER BY created_at, id LIMIT ?

-- name: CANCEL
UPDATE {{prefix}}task
SET status = 'CANCELLED', claim_token = NULL, lease_until = NULL, updated_at = CURRENT_TIMESTAMP(6)
WHERE id = ? AND status IN ('PENDING', 'RUNNING', 'FAILED')

-- name: RESCHEDULE
UPDATE {{prefix}}task
SET status = 'PENDING', next_execute_at = ?, attempts = 0,
    claim_token = NULL, lease_until = NULL, last_error = NULL, updated_at = CURRENT_TIMESTAMP(6)
WHERE id = ? AND status IN ('PENDING', 'FAILED', 'CANCELLED')

-- name: RETRY_FAILED
UPDATE {{prefix}}task
SET status = 'PENDING', next_execute_at = CURRENT_TIMESTAMP(6), attempts = 0,
    claim_token = NULL, lease_until = NULL, last_error = NULL, updated_at = CURRENT_TIMESTAMP(6)
WHERE id = ? AND status = 'FAILED'

-- name: INSERT_OPERATION
INSERT INTO {{prefix}}task_operation(id, created_at, updated_at, task_id, operator_id, reason)
VALUES (?, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), ?, ?, ?)

-- name: COUNT_PENDING
SELECT COUNT(*) FROM {{prefix}}task WHERE status IN ('PENDING', 'RUNNING')

-- name: COUNT_FAILED
SELECT COUNT(*) FROM {{prefix}}task WHERE status = 'FAILED'

-- name: OLDEST_PENDING
SELECT COALESCE(TIMESTAMPDIFF(SECOND, MIN(created_at), CURRENT_TIMESTAMP(6)), 0) FROM {{prefix}}task WHERE status IN ('PENDING', 'RUNNING')
