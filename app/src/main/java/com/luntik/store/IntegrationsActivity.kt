            PayRow("Копия профиля") { note = plans.exportBackup() }
        }
        if (plans.storeActive()) {
            val extra = plans.familyPrice()
            if (!plans.familyPaid) PayRow("Семейный слот +$extra LC") {
                startPay("Семейный слот", extra, "family-${System.currentTimeMillis() / 86_400_000L}") { plans.unlockFamily(); note = "слот оплачен" }
            } else PayRow("Показать семейный код") { note = plans.familyCode() }
            BasicTextField(code, { code = it }, textStyle = TextStyle(color = Color.White), cursorBrush = SolidColor(Color.White))
            PayRow("Вставить семейный код") { note = if (plans.takeFamily(code)) "подписка на этом устройстве" else "код не тот" }
        }
