# FinTrack - Personal Finance Tracker

A beautiful Android app to track your money, set goals, and stay on top of your finances.

## 📱 Screenshots

<p align="center">
  <img src="https://github.com/user-attachments/assets/b9e8f234-6ff6-42d1-8b13-69b0a86159e3" width="230" style="margin:0 12px;"/>
  <img src="https://github.com/user-attachments/assets/ddf33ad4-a62b-4cc2-892e-13da0950aba0" width="230" style="margin:0 12px;"/>
  <img src="https://github.com/user-attachments/assets/78007383-8162-4a75-bcb1-d8d24d4134bf" width="230" style="margin:0 12px;"/>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/b77ab891-9891-4afe-a44c-caee39958173" width="230" style="margin:0 12px;"/>
  <img src="https://github.com/user-attachments/assets/1a17ba26-05a4-4b5e-9e42-5f278935f781" width="230" style="margin:0 12px;"/>
</p>

## Features

### Core Features
- **Add Transactions** - Income and expenses with categories
- **Transaction History** - Search, filter, and manage all transactions
- **Spending Insights** - Charts showing where your money goes
- **Goal Tracking** - Set savings goals and track progress

### Security
- **Biometric Lock** - Unlock with fingerprint or face recognition
- **PIN Backup** - 6-digit PIN if biometric fails
- **Auto-lock** - App locks when you leave it

### Multi-Currency
- 14 currencies supported (USD, INR, EUR, GBP, JPY, CAD, AUD, CNY, SGD, AED, KRW, RUB, BRL, ZAR)
- Real-time exchange rates
- Change currency anytime

### Backup & Restore
- **Export Data** - Save all transactions to a JSON file
- **Import Data** - Restore from backup file
- **Works even after app deletion** - Keep your backup file safe, import anytime

### Profile
- Add profile photo from camera or gallery
- Edit name, email, bio

## Tech Stack

- Kotlin
- AndroidX Biometric
- SharedPreferences with Gson
- Custom charts (DonutChart, BarChart)
- Glass-morphism UI design

## How to Use

### First Time Setup
1. Install the app
2. Set a backup PIN in Settings → Security
3. Enable biometric lock if desired
4. Start adding transactions

### Adding a Transaction
1. Tap the + button
2. Select Income or Expense
3. Enter amount and title
4. Choose a category
5. Save

### Exporting Data
1. Go to Settings
2. Tap Export Data
3. File saves to Downloads folder
4. Share or keep as backup

### Importing Data
1. Go to Settings
2. Tap Import Data
3. Select your backup JSON file
4. Confirm import

### Changing Currency
1. Go to Settings
2. Tap Currency
3. Search and select your currency
4. All amounts convert automatically

### Setting Up Biometric Lock
1. Go to Settings → Security
2. Set a PIN first
3. Enable Biometric Lock
4. App will lock automatically

## Categories

### Expenses
- 🍔 Food
- 🛍 Shopping
- ⛽ Transport
- 📺 Bills
- 🏥 Health
- 🎓 Education
- 🎮 Entertainment
- 🏠 Rent
- 📦 Other

### Income
- 💰 Salary
- 💸 Freelance
- 🎁 Gift
- 📈 Investment
- 🏦 Bank
- 💡 Bonus
- 📦 Other

## Requirements

- Android 7.0 (API 24) or higher
- 50MB free space

## Installation

Download the APK and install. No special permissions needed except:
- Camera (for profile photo)
- Storage (for export/import)
- Fingerprint (for biometric lock)

## Future Plans

- **SMS Parsing** - Auto-detect transactions from PhonePe, GPay, Paytm notifications
- **Daily Reminders** - Get notified to add expenses
- **Budget Alerts** - Warn when spending exceeds category limits
- **Cloud Backup** - Google Drive sync
- **PDF Reports** - Monthly and yearly expense reports
- **Receipt Scanning** - Take photo of receipt, auto-fill details

## Known Issues

None. App is stable and production ready.

## Support

For issues or feature requests, create an issue on GitHub.

## License

Developed as part of internship assignment.

---

**Made with ❤️**

Download FinTrack and take control of your finances today.
