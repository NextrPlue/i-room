def check_violation(helmet_count: int, seatbelt_count: int, total_workers: int) -> dict:
    """
    집계된 helmet/seatbelt 수와 총 근로자 수를 비교해 미착용 여부 판별
    """
    detected_with_ppe = min(helmet_count, seatbelt_count)
    violation = total_workers > detected_with_ppe
    reason = []

    if violation:
        reason.append(
            f"Total workers={total_workers}, but PPE detected={detected_with_ppe}"
        )

# return violation만 return 해도됨!
    return {
        "helmet_on": helmet_count,
        "seatbelt_on": seatbelt_count,
        "total_workers": total_workers,
        "violation": violation,
        "reason": reason,
    }
