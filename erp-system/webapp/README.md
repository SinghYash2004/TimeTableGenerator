# ERP Web Module

This web module mirrors CLI flows and adds a decision-support dashboard UI.

## Included
- Login (`users` table + auto-seeded admin)
- Dashboard with KPI cards and charts
- Timetable operations:
  - Generate (Greedy / Graph)
  - Detect conflicts
  - Export CSV
- Department cards with budget risk indicators
- Faculty and Rooms pages
- AI Optimization, Reports, Settings pages

## Run
From project root:

```powershell
powershell -ExecutionPolicy Bypass -File .\run-webapp.ps1 -DbUser root -DbPassword "YOUR_PASSWORD" -Port 8081
```

Open:
- `http://localhost:8081/login`

Default admin:
- `admin / admin123`
