#!/usr/bin/env python3
from dataclasses import dataclass
from enum import Enum

class PrimaryAction(str, Enum):
    WORK = "WORK"
    CLOSE_NOW = "CLOSE_NOW"
    NOOP = "NOOP"

class BackupAction(str, Enum):
    WAIT_PRIMARY = "WAIT_PRIMARY"
    RECONCILE_EXISTING_END = "RECONCILE_EXISTING_END"
    SEND_NORMAL_END = "SEND_NORMAL_END"
    NOOP = "NOOP"

@dataclass(frozen=True)
class DeliverySnapshot:
    offset_minutes: int
    closed: bool
    end_ack_persisted: bool
    end_found_in_gmail: bool

USEFUL_WORK_MINUTES = 22
NOMINAL_END_MINUTES = 25
BACKUP_EARLIEST_MINUTES = 26
HARD_GUARD_MINUTES = 29

def primary_action(snapshot: DeliverySnapshot) -> PrimaryAction:
    if snapshot.closed or snapshot.end_ack_persisted:
        return PrimaryAction.NOOP
    if snapshot.offset_minutes >= USEFUL_WORK_MINUTES:
        return PrimaryAction.CLOSE_NOW
    return PrimaryAction.WORK

def backup_action(snapshot: DeliverySnapshot) -> BackupAction:
    if snapshot.closed or snapshot.end_ack_persisted:
        return BackupAction.NOOP
    if snapshot.offset_minutes < BACKUP_EARLIEST_MINUTES:
        return BackupAction.WAIT_PRIMARY
    if snapshot.end_found_in_gmail:
        return BackupAction.RECONCILE_EXISTING_END
    return BackupAction.SEND_NORMAL_END
