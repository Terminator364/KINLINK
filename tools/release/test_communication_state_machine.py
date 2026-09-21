#!/usr/bin/env python3
from communication_state_machine import (
    BackupAction,
    DeliverySnapshot,
    PrimaryAction,
    backup_action,
    primary_action,
)

def snap(minute, *, closed=False, persisted=False, found=False):
    return DeliverySnapshot(
        offset_minutes=minute,
        closed=closed,
        end_ack_persisted=persisted,
        end_found_in_gmail=found,
    )

def run():
    assert primary_action(snap(0)) == PrimaryAction.WORK
    assert primary_action(snap(21)) == PrimaryAction.WORK
    assert primary_action(snap(22)) == PrimaryAction.CLOSE_NOW
    assert primary_action(snap(25)) == PrimaryAction.CLOSE_NOW
    assert primary_action(snap(22, persisted=True)) == PrimaryAction.NOOP

    # Backup is forbidden from stealing normal close.
    for minute in range(0, 26):
        assert backup_action(snap(minute)) == BackupAction.WAIT_PRIMARY

    # Crash after Gmail accepted END but before GitHub persisted receipt:
    # reconcile; never send a duplicate.
    assert backup_action(snap(26, found=True)) == BackupAction.RECONCILE_EXISTING_END
    assert backup_action(snap(29, found=True)) == BackupAction.RECONCILE_EXISTING_END

    # Only truly missing END may be sent after the nominal window.
    assert backup_action(snap(26)) == BackupAction.SEND_NORMAL_END
    assert backup_action(snap(29)) == BackupAction.SEND_NORMAL_END

    # Any persisted/closed delivery is a strict no-op.
    assert backup_action(snap(26, persisted=True)) == BackupAction.NOOP
    assert backup_action(snap(29, closed=True)) == BackupAction.NOOP

    print("communication-state-machine-scenarios: PASS")

if __name__ == "__main__":
    run()
