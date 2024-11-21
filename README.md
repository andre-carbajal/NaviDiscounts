# NaviDiscounts

NaviDiscounts is a telegram bot that helps you find discounts on your favorite products on certain websites.

## Technologies

- [Kotlin](https://kotlinlang.org/)
- [Java Telegram](https://github.com/rubenlagus/TelegramBots)
- [PostgreSQL](https://www.postgresql.org/)
- [Jsoup](https://jsoup.org/)
- [Selenium](https://www.selenium.dev/)

## Supported Websites

- [Mifarma](https://www.mifarma.com.pe/)
- [InkaFarma](https://inkafarma.pe/)

## Commands

- `/start` - Get the list of commands.
- `/add` - Add a URL to the list of requests.
- `/delete` - Delete a URL from the list of requests.
- `/list` - Get the list of requests.
- `/stop` - Stop receiving product information and delete all added products.
- `/exe` - Execute the [scheduler](src/main/kotlin/net/andrecarbajal/telegramdiscountsbot/bot/Scheduler.kt).

## How to build

### Production mode

Run the project in production mode with the following command:

   ```bash
   java -jar build/libs/NaviDiscounts-{version}.jar \
    --spring.profiles.active=prod \
    --spring.datasource.url=jdbc:postgresql://{YOUR_DATABASE_HOST}:{YOUR_DATABASE_PORT}/{YOUR_DATABASE_NAME} \
    --spring.datasource.username={YOUR_DATABASE_USERNAME} \
    --spring.datasource.password={YOUR_DATABASE_PASSWORD} \
    --spring.bot.token=${TELEGRAM_BOT_TOKEN} \
    --spring.bot.scheduler.enabledExeCommand={false|true} \
    --spring.bot.scheduler.timeZone={TIME_ZONE:UTC} \
    --spring.bot.scheduler.executionTime={EXECUTION_TIME:00:00}
   ```

### Development Mode

Run the project in development mode with the following command:

   ```bash
   java -jar build/libs/NaviDiscounts-{version}.jar \
    --spring.bot.token=${TELEGRAM_BOT_TOKEN} \
    --spring.bot.scheduler.timeZone={TIME_ZONE:UTC} \
    --spring.bot.scheduler.executionTime={EXECUTION_TIME:00:00}
   ```

## how to use

1. Add the bot to your telegram account
2. Send the command `/start` to the bot
3. Send the command `/add` to the bot
4. Send the URL of the product you want to track
5. Wait for the bot to send you a message with the discount information
6. If you want a command to execute the scheduler, you can enable it withe the `enabledExeCommand` parameter
7. Enjoy!

## Contributions

If you want to contribute to the project, you can create a pull request with the changes you want to make. If you have
any questions or issue, you can create an [issue](https://github.com/andre-carbajal/NaviDiscounts/issues).

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.
