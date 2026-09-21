# KINLINK — Mobile Vault W2 core

This is the first executable W2 policy block.

The Mobile Vault now has explicit economic envelopes:
- total plan;
- normal allowance;
- protected reserve;
- rescue allowance;
- critical-interactive allowance.

The policy is fail-safe:
- invalid configuration => UNKNOWN and no autonomous mobile action;
- unknown cycle usage => UNKNOWN and no autonomous mobile action;
- expired/exhausted => no spend;
- protected reserve suspends autonomous normal actions;
- rescue zone permits only rescue/critical classes;
- critical zone permits only critical-interactive class.

This commit does **not** claim operator balance discovery, multi-SIM accounting or a complete Mobile Vault UI. Those remain separate W2 gates. It also does not enable paid probes or Strong Stabilizer traffic.
