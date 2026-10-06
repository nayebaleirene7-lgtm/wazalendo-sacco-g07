# Wazalendo SACCO Management System
**Group One (G07)**

## How to compile and run

```bash
cd wazalendo-sacco
javac -encoding UTF-8 -d bin $(find src -name "*.java")
java -cp bin ug.ac.vu.g07.core.Main
```

Data files are stored in the `data/` folder and are created automatically.

## Team & Modules

| Member | Module | Role |
|--------|--------|------|
| Ninsiima Douglas | Members | Team Leader |
| Mugeni Felestar Auma | Savings | Client Liaison |
| Ssenvume Andrew | Shares | Storage Lead |
| Ssempewo Brian | Loans | Integration Lead |
| Nayebale Irene | Loan Repayments & Arrears | Test Lead |
| Kazi Fredrick | Meetings & Fines | Documentation Lead |
| Kwemboi Ramazan | Reports / System Summary | Quality Support |

## Features

- Shared abstract class `Record` + interface `Reportable`
- Robust InputHelper (never crashes)
- File storage that survives missing/damaged files
- Members, Savings, Shares, Loans, Meetings & Fines
- 3x loan eligibility rule using live savings + share capital
- 10% flat interest, 2% late penalty
- Member Statement combining all modules
- All IDs start with G07-
