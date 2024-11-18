# NaviDiscounts

NaviDiscounts is a telegram bot that helps you find discounts on your favorite products on certain websites.

## Website Supported

- [Mifarma](https://www.mifarma.com.pe/)
- [InkaFarma](https://inkafarma.pe/)

## Commands

- `/start` - Get the list of commands.
- `/add` - Add a URL to the list of requests.
- `/delete` - Delete a URL from the list of requests.
- `/list` - Get the list of requests.
- `/stop` - Stop receiving product information and delete all added products.
- `/exe` - Execute the [scheduler](src/main/kotlin/net/andrecarbajal/telegramdiscountsbot/bot/Scheduler.kt). (only in
  development)

## Technologies

- [Kotlin](https://kotlinlang.org/)
- [Java Telegram](https://github.com/rubenlagus/TelegramBots)
- [PostgreSQL](https://www.postgresql.org/)
- [Jsoup](https://jsoup.org/)
- [Selenium](https://www.selenium.dev/)

## How to build

1. Clone the repository.
2. Build the project with `./gradlew build`.
3. Run the project with:
   ```bash
   java -jar build/libs/NaviDiscounts-{x.y}.jar --spring.profiles.active=prod --spring.datasource.url=jdbc:postgresql://{YOUR_DATABASE_HOST}:{YOUR_DATABASE_PORT}/{YOUR_DATABASE_NAME} --spring.datasource.username={YOUR_DATABASE_USERNAME} --spring.datasource.password={YOUR_DATABASE_PASSWORD} --spring.telegram.bot.token={YOUR_TELEGRAM_TOken}
   ```

## how to use

1. Add the bot to your telegram account
2. Send the command `/start` to the bot
3. Send the command `/add` to the bot
4. Send the URL of the product you want to track
5. Wait for the bot to send you a message with the discount information
6. Enjoy!

## Contributions

If you want to contribute to the project, you can create a pull request with the changes you want to make. If you have
any questions or issue, you can create an [issue](https://github.com/andre-carbajal/NaviDiscounts/issues).