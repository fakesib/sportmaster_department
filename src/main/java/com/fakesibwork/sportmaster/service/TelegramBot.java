package com.fakesibwork.sportmaster.service;

import com.fakesibwork.sportmaster.config.BotConfig;
import com.fakesibwork.sportmaster.model.Day;
import com.fakesibwork.sportmaster.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;


@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired private UserService userService;

    @Autowired private DayService dayService;

    @Autowired private TransactionService transactionService;

    @Value("${group.token}")
    private String adminGroupToken;

    BotConfig botConfig;

    Map<Long, String> userMap = new HashMap<>();

    Map<Long, List<String>> adminMap = new HashMap<>();

    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/mysales", "Мои продажи\uD83D\uDC64"));
        listOfCommands.add(new BotCommand("/code", "Получить код\uD83D\uDCEC"));
        listOfCommands.add(new BotCommand("/help", "Если потерялись\uD83C\uDD98"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
        }
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasChatJoinRequest()){
            sendMessage(update.getChatJoinRequest().getUserChatId(), "Приветствую тебя в боте СМ Мои продажи, ожидайте принятие вашей заявки в группу администратором!");
        } else if (update.hasMessage() && update.getMessage().hasText()){
            long userId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();

            if (messageText.equals("/code")){
                sendMessage(userId, "Твой userId: <code>" + userId + "</code>");
            } else if (messageText.equals("/resetdaydata") && userService.getRole(userId).equals("ADMIN")) {
                resetDayData();
                sendMessage(userId, "Данные за сегодня обновлены");
            } else if (messageText.equals("/runupdatedata") && userService.getRole(userId).equals("ADMIN")) {
                runUpdateData();
                sendMessage(userId, "Таймер запущен");
            } else if (messageText.equals("/mysales") && userService.getRole(userId).equals("SELLER")) {
                mySalesCommand(userId);
            } else if (userMap.get(userId) != null) {
                dataProcessing(userId, messageText, userMap.get(userId));
            } else if (messageText.equals("/admin") && userService.getRole(userId).equals("ADMIN")) { // admin panel
                adminPanel(userId);
            } else if (adminMap.get(userId).get(0).equals("DELETE") && userService.getRole(userId).equals("ADMIN")) {
                if (isInteger(messageText)){
                    deleteStaffConfirmCommand(userId, messageText);
                } else {
                    notNumberAdminCommand(userId);
                }
            } else if (adminMap.get(userId).get(0).equals("ADD-ROLE-ID") && userService.getRole(userId).equals("ADMIN")) {
                if (isLong(messageText)){
                    nameAddStaffCommand(userId, messageText);
                } else {
                    notNumberAdminCommand(userId);
                }
            } else if (adminMap.get(userId).get(0).equals("ADD-ROLE-ID-NAME") && userService.getRole(userId).equals("ADMIN")) {
                confirmAddStaffCommand(userId, messageText);
            } else if (adminMap.get(userId).get(0).equals("DELETE-TODAY") && userService.getRole(userId).equals("ADMIN")) {
                if (isInteger(messageText)){
                    deleteTodayStaffConfirmCommand(userId, messageText);
                } else {
                    notNumberAdminCommand(userId);
                }
            } else if (adminMap.get(userId).get(0).equals("ADD-TODAY") && userService.getRole(userId).equals("ADMIN")) {
                if (isLong(messageText)){
                    addTodayStaffConfirmCommand(userId, messageText);
                } else {
                    notNumberAdminCommand(userId);
                }
            }
        } else if (update.hasCallbackQuery()){
            String callBackData = update.getCallbackQuery().getData();
            long userId = update.getCallbackQuery().getMessage().getChatId();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();

            switch (callBackData){
                case "USER_BUTTON" ->
                    userPanel(userId, messageId);
                case "FAST_BUTTON" ->
                    fastCommand(userId, messageId);
                case "CART_BUTTON" ->
                    cartCommand(userId, messageId);
                case "HERE_BUTTON" ->
                    hereCommand(userId, messageId);
                case "DELIVERY_BUTTON" ->
                    deliveryCommand(userId, messageId);
                case "MOBILE_BUTTON" ->
                    mobileCommand(userId, messageId);
                case "JUST_MOBILE_BUTTON" ->
                    justMobileCommand(userId, messageId);
                case "EMAIL_BUTTON" ->
                    emailCommand(userId, messageId);
                case "MONTH_SALES_USER_BUTTON" ->
                    myMonthSales(userId, messageId);
                case "MY_SALES_BUTTON" ->
                    mySalesCommand(userId, messageId);
                case "DELETE_TRANSACTION_BUTTON" ->
                    deleteTransactionCommand(userId, messageId);

                // admin buttons
                case "STAFF_BUTTON" ->
                    staffCommand(userId, messageId);
                case "ADMIN_BUTTON" ->
                    adminPanel(userId, messageId);
                case "DELETE_STAFF_BUTTON" ->
                    deleteStaffCommand(userId, messageId);
                case "YES_DELETE_STAFF_BUTTON" ->
                    yesDeleteStaffCommand(userId, messageId);
                case "ADD_STAFF_BUTTON" ->
                    addStaffCommand(userId, messageId);
                case "SELLER_ADD_STAFF_BUTTON" ->
                    userRoleAddStaffCommand(userId, messageId, "SELLER");
                case "CASHIER_ADD_STAFF_BUTTON" ->
                    userRoleAddStaffCommand(userId, messageId, "CASHIER");
                case "CONFIRM_ADD_STAFF_BUTTON" ->
                    successAddStaffCommand(userId, messageId, adminMap.get(userId));
                case "TODAY_STAFF_BUTTON" ->
                    todayStaffCommand(userId, messageId);
                case "DELETE_TODAY_STAFF_BUTTON" ->
                    deleteTodayStaffCommand(userId, messageId);
                case "YES_DELETE_TODAY_STAFF_BUTTON" ->
                    yesDeleteTodayStaffCommand(userId, messageId);
                case "ADD_TODAY_STAFF_BUTTON" ->
                    addTodayStaffCommand(userId, messageId);
                case "DATA_BUTTON" ->
                    dataCommand(userId, messageId);
                case "UPDATE_DATA_BUTTON" ->
                    updateDataCommand(userId, messageId);
                case "YES_UPDATE_DATA_BUTTON" ->
                    yesUpdateDataCommand(userId, messageId);
                case "TODAY_DATA_BUTTON" ->
                    todayDataCommand(userId, messageId);
                case "MONTH_DATA_BUTTON" ->
                    monthDataCommand(userId, messageId);
            }
        }
    }

    private void monthDataCommand(long userId, long messageId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(makeButton("Назад ⬅", "DATA_BUTTON"));
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);

        String text = userService.getStoreDataByChat(userId);

        sendEditTextMessageWithButtons(userId, text, messageId, markup);
    }

    public void runUpdateData() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int hour = java.time.LocalTime.now().getHour();
                switch (hour){
                    case 9 ->   // Обновляются общие данные
                                // TODO админы выбирают людей
                        morningRoutine();
                    case 13, 21 ->  // Уведомления для сотрудников
                        notifications();
                    case 14, 18 -> // Присылается в админский чат данные
                        sendDataToAdminGroup();
                    case 22 ->  // Присылаются общие данные
                                // Скидывается топ
                                // Добавляются данные в месяц
                                // Обновляются транзакции
                        eveningRoutine();
                }
            }
        }, 0, 3600000); // Проверка каждый час
    }

    private void notifications() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(makeButton("Записать данные ✍", "USER_BUTTON"));
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);
        List<Long> sellers = dayService.getSellersChats();
        for (int i = 0; i < sellers.size(); i++) {
            sendMessageWithButtons(sellers.get(i), "В скором времени будет опубликован отчёт, просьба записать данные!", markup);
        }
    }

    private void morningRoutine() {
        resetDayData();

//        List<Day> allAdmin = dayService.getAllAdmin();
//        for (int i = 0; i < allAdmin.size(); i++) {
//            sendMessageWithButtons(allAdmin.get(i).getChat(), "Выберите продавцов на день");
//        }
    }

    private void eveningRoutine() {
        // данные за сегодня
        List<Day> stores = dayService.getAllStores();
        for (int i = 0; i < stores.size(); i++) {
            dayService.updateStoreData(stores.get(i).getChat());
        }
        sendMessage(Long.parseLong(adminGroupToken), dayService.getBetweenDataToAdmin());
        List<Long> day = dayService.getAllUsersChat();
        sendMessage(Long.parseLong(adminGroupToken), userService.updateDataByDay(day)); // данные за месяц

        for (int i = 0; i < stores.size(); i++) {
            topCommand(stores.get(i).getChat());
        }

        transactionService.resetTransactions();
    }

    private void topCommand(long chatId) {
        List<String> tops = dayService.getTop(chatId);
        StringBuilder builder = new StringBuilder();
        builder.append("Топ за сегодня \uD83C\uDFC6 \n");
        for (int i = 0; i < tops.size(); i++) {
            builder.append(tops.get(i));
        }
        sendMessage(chatId, builder.toString());
    }

    private void sendDataToAdminGroup(){
        List<Day> allStores = dayService.getAllStores();
        for (int i = 0; i < allStores.size(); i++) {
            dayService.updateStoreData(allStores.get(i).getChat());
        }
        sendMessage(Long.parseLong(adminGroupToken), dayService.getBetweenDataToAdmin());
    }

    private void resetDayData(){
        dayService.resetDayData();
    }

    // USER COMMANDS

    private void deleteTransactionCommand(long userId, long messageId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(makeButton("Назад ⬅", "MY_SALES_BUTTON"));
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);
        userMap.put(userId, "delete-transaction");
        sendEditTextMessageWithButtons(userId, "Введите номер транзакции ввиде: \ne7b42753-f3e3-4987-b69a-03a51db478e7", messageId, markup);
    }

    private void myMonthSales(long userId, long messageId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(makeButton("Назад ⬅", "MY_SALES_BUTTON"));
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);
        sendEditTextMessageWithButtons(userId, userService.getMySales(userId), messageId, markup);
    }

    private void mySalesCommand(long userId){
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline.add(makeButton("К продажам \uD83D\uDE80", "USER_BUTTON"));
        rowInline1.add(makeButton("Продажи за месяц \uD83D\uDCC5", "MONTH_SALES_USER_BUTTON"));
        rowInline2.add(makeButton("Удалить транзакцию \uD83D\uDDD1", "DELETE_TRANSACTION_BUTTON"));
        rowsInline.add(rowInline);
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        markup.setKeyboard(rowsInline);
        sendMessageWithButtons(userId, dayService.getMySales(userId), markup);
    }

    private void mySalesCommand(long userId, long messageId){
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline.add(makeButton("К продажам \uD83D\uDE80", "USER_BUTTON"));
        rowInline1.add(makeButton("Продажи за месяц \uD83D\uDCC5", "MONTH_SALES_USER_BUTTON"));
        rowInline2.add(makeButton("Удалить транзакцию \uD83D\uDDD1", "DELETE_TRANSACTION_BUTTON"));
        rowsInline.add(rowInline);
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        markup.setKeyboard(rowsInline);
        sendEditTextMessageWithButtons(userId, dayService.getMySales(userId), messageId, markup);
    }

    private void notNumberUserCommand(long userId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(makeButton("Главное меню ⬅", "USER_BUTTON"));
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);
        sendMessageWithButtons(userId, "⛔Введите правильное значение ввиде числа без точек, тире, пробелов и тд.⛔", markup);
    }

    private void dataProcessing(long userId, String sum, String type) {
        if (type.equals("null")){
            userPanel(userId);
        } else if (type.equals("delete-transaction")) {
            dayService.deleteDataByTransaction(transactionService.deleteTransaction(sum));
            sendMessage(userId, "Транзакция удалена");
        } else if (isInteger(sum) && dayService.getUserById(userId)) {
            dayService.addData(userId, sum, type);
            String transaction = transactionService.createTransaction(userId, sum, type);
            sendMessage(userId, transaction);
            userPanel(userId);
        } else if (!isInteger(sum)) {
            notNumberUserCommand(userId);
        } else {
            sendMessage(userId, "Вы не работаете сегодня!");
        }
    }

    private void emailCommand(long userId, long messageId){
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(makeButton("Назад ⬅", "USER_BUTTON"));
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);
        userMap.put(userId, "email");
        String text = "Введите количество МП без точек, тире, пробелов и тд.";
        sendEditTextMessageWithButtons(userId, text, messageId, markup);
    }

    private void justMobileCommand(long userId, long messageId){
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(makeButton("Назад ⬅", "USER_BUTTON"));
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);
        userMap.put(userId, "just-mobile");
        String text = "Введите количество МП без точек, тире, пробелов и тд.";
        sendEditTextMessageWithButtons(userId, text, messageId, markup);
    }

    private void mobileCommand(long userId, long messageId){
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline.add(makeButton("Просто МП \uD83D\uDCF1", "JUST_MOBILE_BUTTON"));
        rowInline1.add(makeButton("МП E-mail \uD83D\uDCE8", "EMAIL_BUTTON"));
        rowInline2.add(makeButton("Назад ⬅", "USER_BUTTON"));
        rowsInline.add(rowInline);
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        markup.setKeyboard(rowsInline);
        String text = "<b>Вы в разделе: Мобильное приложение \uD83D\uDCF1 \n\n" +
                "Просто МП</b> - установили мобильное приложение клиенту\n" +
                "<b>МП E-mail</b> - установили приложение и подтвердили почту в профиле";
        sendEditTextMessageWithButtons(userId, text, messageId, markup);
    }

    private void hereCommand(long userId, long messageId){
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(makeButton("Назад ⬅", "USER_BUTTON"));
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);
        userMap.put(userId, "here");
        String text = "Введите сумму покупки без точек, тире, пробелов и тд.";
        sendEditTextMessageWithButtons(userId, text, messageId, markup);
    }

    private void deliveryCommand(long userId, long messageId){
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(makeButton("Назад ⬅", "USER_BUTTON"));
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);
        userMap.put(userId, "delivery");
        String text = "Введите сумму покупки без точек, тире, пробелов и тд.";
        sendEditTextMessageWithButtons(userId, text, messageId, markup);
    }

    private void cartCommand(long userId, long messageId){
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline.add(makeButton("Самовывоз \uD83D\uDCCD", "HERE_BUTTON"));
        rowInline1.add(makeButton("Доставка \uD83D\uDE9A", "DELIVERY_BUTTON"));
        rowInline2.add(makeButton("Назад ⬅", "USER_BUTTON"));
        rowsInline.add(rowInline);
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        markup.setKeyboard(rowsInline);
        String text = "<b>Вы в разделе: Единая корзина \uD83D\uDDD1 \n\n" +
                "Самовывоз </b>- интернет покупка из магазина в котором вы работаете\n" +
                "<b>Доставка </b>- доставка в любом виде, оформление заказа в другой магазин";
        sendEditTextMessageWithButtons(userId, text, messageId, markup);
    }

    private void fastCommand(long userId, long messageId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(makeButton("Назад ⬅", "USER_BUTTON"));
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);
        userMap.put(userId, "fast");
        String text = "Введите сумму покупки без точек, тире, пробелов и тд.";
        sendEditTextMessageWithButtons(userId, text, messageId, markup);
    }

    private void userPanel(long userId){
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
        rowInline1.add(makeButton("Единая корзина \uD83D\uDDD1", "CART_BUTTON"));
        rowInline2.add(makeButton("Мобильное приложение \uD83D\uDCF1", "MOBILE_BUTTON"));
        rowInline3.add(makeButton("Быстрая продажа \uD83D\uDCB8", "FAST_BUTTON"));
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        rowsInline.add(rowInline3);
        markup.setKeyboard(rowsInline);
        userMap.put(userId, "null");
        String text = "Выберите необходимый пункт:\n<i>*Чтобы управлять данными, нажмите /mysales</i>";
        sendMessageWithButtons(userId, text, markup);
    }

    private void userPanel(long userId, long messageId){
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
        rowInline1.add(makeButton("Единая корзина \uD83D\uDDD1", "CART_BUTTON"));
        rowInline2.add(makeButton("Мобильное приложение \uD83D\uDCF1", "MOBILE_BUTTON"));
        rowInline3.add(makeButton("Быстрая продажа \uD83D\uDCB8", "FAST_BUTTON"));
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        rowsInline.add(rowInline3);
        markup.setKeyboard(rowsInline);
        userMap.put(userId, "null");
        String text = "<b>Выберите необходимый пункт:</b>\n<i>*Чтобы управлять данными, нажмите /mysales</i>";
        sendEditTextMessageWithButtons(userId, text, messageId, markup);
    }

    // ADMIN COMMANDS

    private void todayDataCommand(long userId, long messageId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(makeButton("Назад ⬅", "DATA_BUTTON"));
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);

        String text = dayService.getTodayStoreDataByChat(userId);

        sendEditTextMessageWithButtons(userId, text, messageId, markup);
    }

    private void yesUpdateDataCommand(long userId, long messageId){
        dayService.updateStoreData(userId);
        sendEditTextMessage(userId, "Данные обновлены", messageId);
        adminPanel(userId);
    }

    private void updateDataCommand(long userId, long messageId){
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline.add(makeButton("Подтвердить ✅", "YES_UPDATE_DATA_BUTTON"));
        rowInline1.add(makeButton("Назад ⬅", "DATA_BUTTON"));
        rowsInline.add(rowInline);
        rowsInline.add(rowInline1);
        markup.setKeyboard(rowsInline);
        String text = "Вы уверены что хотите обновить данные прямо сейчас? Данные автоматически обновляются в 14:30, 18:30, 22:30!";
        sendEditTextMessageWithButtons(userId, text, messageId, markup);
    }

    private void dataCommand(long userId, long messageId){
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
        rowInline.add(makeButton("Сегодня ☀", "TODAY_DATA_BUTTON"));
        rowInline.add(makeButton("Месяц \uD83D\uDCC5", "MONTH_DATA_BUTTON"));
        rowInline1.add(makeButton("Данные сотрудника ❌", "NOT_YET"));
        rowInline2.add(makeButton("Обновить \uD83D\uDD04", "UPDATE_DATA_BUTTON"));
        rowInline3.add(makeButton("Назад ⬅", "ADMIN_BUTTON"));
        rowsInline.add(rowInline);
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        rowsInline.add(rowInline3);
        markup.setKeyboard(rowsInline);
        sendEditTextMessageWithButtons(userId, "Выберите необходимые данные:", messageId, markup);
    }

    private void addTodayStaffConfirmCommand(long userId, String messageText) {
        User user = userService.getUserById(messageText);
        userMap.put(user.getChat(), "null");
        sendMessage(userId, dayService.addUserByChat(user.getChat(), user.getName(), user.getStore(), user.getRole()));
        adminPanel(userId);
    }

    private void addTodayStaffCommand(long userId, long messageId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(makeButton("Назад ⬅", "TODAY_STAFF_BUTTON"));
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);
        List<String> list = new ArrayList<>();
        list.add(0, "ADD-TODAY");
        adminMap.put(userId, list);
        sendEditTextMessageWithButtons(userId, "Введите id сотрудника для добавления:", messageId, markup);
    }

    private void yesDeleteTodayStaffCommand(long userId, long messageId) {
        userMap.remove(Long.parseLong(adminMap.get(userId).get(2)));
        sendEditTextMessage(userId, dayService.deleteUser(Integer.parseInt(adminMap.get(userId).get(1))), messageId);
        adminPanel(userId);
    }

    private void deleteTodayStaffConfirmCommand(long userId, String messageText) {
        List<String> user = dayService.canDeleteUser(userId, messageText);
        if (user != null) {
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(makeButton("Да ✅", "YES_DELETE_TODAY_STAFF_BUTTON"));
            rowInline.add(makeButton("Нет ❌", "DELETE_TODAY_STAFF_BUTTON"));
            rowsInline.add(rowInline);
            markup.setKeyboard(rowsInline);
            List<String> list = new ArrayList<>();
            list.add(0,"NULL");
            list.add(1, messageText);
            list.add(2, user.get(3));
            adminMap.put(userId, list);
            String text = "Вы уверены что хотите удалить: \n" + user.get(0) + " - " + user.get(1) + " - " + user.get(2);
            sendMessageWithButtons(userId, text, markup);
        } else  {
            sendMessage(userId, "Вы не можете удалить этого сотрудника! Введите заново id:");
        }
    }

    private void deleteTodayStaffCommand(long userId, long messageId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(makeButton("Назад ⬅", "TODAY_STAFF_BUTTON"));
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);
        List<String> list = new ArrayList<>();
        list.add(0, "DELETE-TODAY");
        adminMap.put(userId, list);
        sendEditTextMessageWithButtons(userId, "Введите Id для удаления сотрудника из сегоднешнего дня:", messageId, markup);
    }

    private void todayStaffCommand(long userId, long messageId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline.add(makeButton("Добавить ➕", "ADD_TODAY_STAFF_BUTTON"));
        rowInline.add(makeButton("Удалить ❌", "DELETE_TODAY_STAFF_BUTTON"));
        rowInline1.add(makeButton("Назад ⬅", "STAFF_BUTTON"));
        rowsInline.add(rowInline);
        rowsInline.add(rowInline1);
        markup.setKeyboard(rowsInline);
        List<String> list = new ArrayList<>();
        list.add(0, "TODAY");
        adminMap.put(userId, list);
        sendEditTextMessageWithButtons(userId, dayService.getAllUsersById(userId), messageId, markup);
    }

    private void successAddStaffCommand(long userId, long messageId, List<String> strings) {
        userService.addNewStaff(userId, strings);
        sendEditTextMessage(userId, "Пользователь добавлен!", messageId);
        adminPanel(userId);
    }

    private void confirmAddStaffCommand(long userId, String messageText) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(makeButton("Да ✅", "CONFIRM_ADD_STAFF_BUTTON"));
        rowInline.add(makeButton("Нет ❌", "ADD_STAFF_BUTTON"));
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);
        List<String> list = new ArrayList<>();
        list.add(0, "NULL");
        list.add(1, adminMap.get(userId).get(1));
        list.add(2, adminMap.get(userId).get(2));
        list.add(3, messageText);
        adminMap.put(userId, list);
        String text = "Подтвердите правильность данных:\n" +
                "Роль: " + adminMap.get(userId).get(1) +
                "\nUserId: " + adminMap.get(userId).get(2) +
                "\nИмя: " + messageText + "\n<i>*Если после первого нажатия на кнопку ДА прошло больше 15 секунд, повторите нажатие пока не появится новое диалоговое окно</i>";
        sendMessageWithButtons(userId, text, markup);
    }

    private void nameAddStaffCommand(long userId, String messageText) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(makeButton("Назад ⬅", "ADD_STAFF_BUTTON"));
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);
        List<String> list = new ArrayList<>();
        list.add(0, "ADD-ROLE-ID-NAME");
        list.add(1, adminMap.get(userId).get(1));
        list.add(2, messageText);
        adminMap.put(userId, list);
        sendMessageWithButtons(userId, "Введите имя и фамилию нового сотрудника:\n\n<i>*Пример: Александр Григорьев</i>", markup);
    }

    private void userRoleAddStaffCommand(long userId, long messageId, String role) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(makeButton("Назад ⬅", "ADD_STAFF_BUTTON"));
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);
        List<String> list = new ArrayList<>();
        list.add(0, "ADD-ROLE-ID");
        list.add(1, role);
        adminMap.put(userId, list);
        sendEditTextMessageWithButtons(userId, "Введите userId нового сотрудника:", messageId, markup);
    }

    private void addStaffCommand(long userId, long messageId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline.add(makeButton("Продавец", "SELLER_ADD_STAFF_BUTTON"));
        rowInline.add(makeButton("Кассир", "CASHIER_ADD_STAFF_BUTTON"));
        rowInline1.add(makeButton("Назад ⬅", "STAFF_BUTTON"));
        rowsInline.add(rowInline);
        rowsInline.add(rowInline1);
        markup.setKeyboard(rowsInline);
        List<String> list = new ArrayList<>();
        list.add(0, "ADD-ROLE");
        adminMap.put(userId, list);
        sendEditTextMessageWithButtons(userId, "Выберите роль нового сотрудника:", messageId, markup);
    }

    private void yesDeleteStaffCommand(long userId, long messageId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(makeButton("Главное меню \uD83D\uDCCB", "ADMIN_BUTTON"));
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);
        sendEditTextMessageWithButtons(userId, userService.deleteUser(adminMap.get(userId)), messageId, markup);
    }

    private void deleteStaffConfirmCommand(long userId, String messageText) {
        List<String> user = userService.canDeleteUser(userId, Integer.parseInt(messageText));
        if (user != null) {
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
            rowInline.add(makeButton("Да ✅", "YES_DELETE_STAFF_BUTTON"));
            rowInline.add(makeButton("Нет ❌", "DELETE_STAFF_BUTTON"));
            rowInline1.add(makeButton("Назад ⬅", "DELETE_STAFF_BUTTON"));
            rowsInline.add(rowInline);
            rowsInline.add(rowInline1);
            markup.setKeyboard(rowsInline);
            List<String> list = new ArrayList<>();
            list.add(0, "DELETE-CONFIRM");
            list.add(1, user.get(0));
            adminMap.put(userId, list);
            sendMessageWithButtons(userId, "Вы уверены что хотите удалить: \n" + user.get(0) + " - " + user.get(1) + " - " + user.get(2), markup);
        } else {
            sendMessage(userId, "Вы не можете удалить этого сотрудника! Введите заново id:");
        }
    }

    private void notNumberAdminCommand(long userId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(makeButton("Назад ⬅", "STAFF_BUTTON"));
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);
        sendMessageWithButtons(userId, "⛔Введите правильное значение ввиде числа без точек, тире, пробелов и тд.⛔", markup);
    }

    private void deleteStaffCommand(long userId, long messageId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(makeButton("Назад ⬅", "STAFF_BUTTON"));
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);
        List<String> list = new ArrayList<>();
        list.add(0, "DELETE");
        adminMap.put(userId, list);
        sendEditTextMessageWithButtons(userId, "Введите id сотрудника для удаления:", messageId, markup);
    }

    private void staffCommand(long chatId, long messageId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline.add(makeButton("Добавить ➕", "ADD_STAFF_BUTTON"));
        rowInline.add(makeButton("Удалить ❌", "DELETE_STAFF_BUTTON"));
        rowInline1.add(makeButton("Сегодня ☀", "TODAY_STAFF_BUTTON"));
        rowInline2.add(makeButton("Назад ⬅", "ADMIN_BUTTON"));
        rowsInline.add(rowInline);
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        markup.setKeyboard(rowsInline);
        String text = userService.getUsersByStore(chatId);

        sendEditTextMessageWithButtons(chatId, text, messageId, markup);
    }

    private void adminPanel(long userId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(makeButton("Данные \uD83D\uDCCA", "DATA_BUTTON"));
        rowInline.add(makeButton("Сотрудники \uD83D\uDC65", "STAFF_BUTTON"));
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);
        sendMessageWithButtons(userId, "Вы на главной странице", markup);
    }

    private void adminPanel(long userId, long messageId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(makeButton("Данные \uD83D\uDCCA", "DATA_BUTTON"));
        rowInline.add(makeButton("Сотрудники \uD83D\uDC65", "STAFF_BUTTON"));
        rowsInline.add(rowInline);
        markup.setKeyboard(rowsInline);
        sendEditTextMessageWithButtons(userId, "Вы на главной странице", messageId, markup);
    }

    private void sendMessage(Long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        sendMessage.setParseMode(ParseMode.HTML);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {

        }
    }

    private InlineKeyboardButton makeButton(String text, String callbackData) {
        var button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    private void sendMessageWithButtons(Long chatId, String textToSend, ReplyKeyboard replyMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        sendMessage.setReplyMarkup(replyMarkup);
        sendMessage.setParseMode(ParseMode.HTML);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {

        }
    }

    private void sendEditTextMessage(Long chatId, String text, long messageId) {
        EditMessageText messageText = new EditMessageText();
        messageText.setChatId(chatId);
        messageText.setText(text);
        messageText.setMessageId((int) messageId);
        messageText.setParseMode(ParseMode.HTML);
        try {
            execute(messageText);
        } catch (TelegramApiException e) {
        }
    }

    private void sendEditTextMessageWithButtons(Long chatId, String text, long messageId,
                                                InlineKeyboardMarkup replyMarkup) {
        EditMessageText messageText = new EditMessageText();
        messageText.setChatId(chatId);
        messageText.setText(text);
        messageText.setReplyMarkup(replyMarkup);
        messageText.setMessageId((int) messageId);
        messageText.setParseMode(ParseMode.HTML);
        try {
            execute(messageText);
        } catch (TelegramApiException e) {
        }
    }

    public static boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isLong(String input) {
        try {
            Long.parseLong(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
