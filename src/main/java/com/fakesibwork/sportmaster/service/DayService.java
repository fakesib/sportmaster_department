package com.fakesibwork.sportmaster.service;

import com.fakesibwork.sportmaster.model.Day;
import com.fakesibwork.sportmaster.model.Transaction;
import com.fakesibwork.sportmaster.model.User;
import com.fakesibwork.sportmaster.repo.DayRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DayService {
    @Autowired
    private DayRepo dayRepo;

    public String getAllUsersById(long userId) {
        int store = dayRepo.findStoreByChat(userId);
        List<Day> dayUsers = dayRepo.findUsersByStore(store);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < dayUsers.size(); i++) {
            builder.append(dayUsers.get(i).getId() + " - " + dayUsers.get(i).getName() + " - " + dayUsers.get(i).getRole() + "\n");
        }
        return "Все кто работают сегодня:\nID - Имя и фамилия - Роль\n" + builder.toString();
    }

    public List<String> canDeleteUser(long userId, String messageText) {
        int deleteUserId = Integer.parseInt(messageText);
        int store = dayRepo.findStoreByChat(userId);
        List<Day> users = dayRepo.findUsersByStore(store);

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == deleteUserId && !users.get(i).getRole().equals("ADMIN") && !users.get(i).getRole().equals("DIRECTOR")){
                List<String> list = new ArrayList<>();
                list.add(String.valueOf(deleteUserId));
                list.add(users.get(i).getName());
                list.add(users.get(i).getRole());
                list.add(String.valueOf(users.get(i).getChat()));
                return list;
            }
        }
        return null;
    }

    public String deleteUser(int id) {
        try {
            dayRepo.deleteUserById(id);
            return "Пользователь успешно удалён";
        } catch (Exception e){
            return "Произошла какая то ошибка";
        }
    }

    public boolean getUserById(long userId) {
        if (dayRepo.existsByChat(userId)){
            return false;
        } else {
            return true;
        }
    }

    public String addUserByChat(long chat, String name, int store, String role) {
        try {
            dayRepo.addUserByChat(chat, name, store, role);
            return "Пользователь добавлен";
        } catch (Exception e) {
            return "Exception add today user";
        }
    }

    public void addData(long userId, String sum, String type) {
        switch (type){
            case "here" ->
                dayRepo.updateHereByChat(Integer.parseInt(sum), userId);
            case "delivery" ->
                dayRepo.updateDeliveryByChat(Integer.parseInt(sum), userId);
            case "just-mobile" ->
                dayRepo.updateMobileByChat(Integer.parseInt(sum), userId);
            case "email" ->
                dayRepo.updateEmailByChat(Integer.parseInt(sum), userId);
            case "fast" ->
                dayRepo.updateFastByChat(Integer.parseInt(sum), userId);
            case "mobile-cash" ->
                dayRepo.updateMobileCashByChat(Integer.parseInt(sum), userId);
        }
    }

    public String getMySales(long userId) {
        Day day = dayRepo.findUserByChat(userId);
        return "Ваши продажи за сегодня:\n" +
                "Самовывозы: " + day.getHere() + "\n" +
                "Доставки: " + day.getDelivery() + "\n" +
                "Просто МП: " + day.getMobile() + "\n" +
                "МП Email: " + day.getEmail() + "\n" +
                "Быстрые продажи: " + day.getFast() + "\n" +
                "МК: " + day.getMobileCash();
    }

    public void deleteDataByTransaction(Transaction transaction) {
        switch (transaction.getType()){
            case "here" ->
                dayRepo.deleteHereByChat(transaction.getSum(), transaction.getChat());
            case "delivery" ->
                dayRepo.deleteDeliveryByChat(transaction.getSum(), transaction.getChat());
            case "just-mobile" ->
                dayRepo.deleteMobileByChat(transaction.getSum(), transaction.getChat());
            case "email" ->
                dayRepo.deleteEmailByChat(transaction.getSum(), transaction.getChat());
            case "fast" ->
                dayRepo.deleteFastByChat(transaction.getSum(), transaction.getChat());
            case "mobile-cash" ->
                dayRepo.deleteMobileCashByChat(transaction.getSum(), transaction.getChat());
        }
    }

    public String getBetweenDataToAdmin() {
        List<Day> stores = dayRepo.findAllStores();

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < stores.size(); i++) {
            builder.append("\n")
                    .append(stores.get(i).getName()).append("\n")
                    .append("ЕК: ").append(stores.get(i).getHere()).append("\n")
                    .append("БП: ").append(stores.get(i).getDelivery()).append("\n")
                    .append("МП: ").append(stores.get(i).getMobile()).append("\n")
                    .append("МП Email: ").append(stores.get(i).getEmail()).append("\n")
                    .append("Быстрые: ").append(stores.get(i).getFast()).append("\n")
                    .append("МК: ").append(stores.get(i).getMobileCash());
        }

        return "<b>Промежуточные данные:</b>\n" + builder.toString();
    }

    public void resetDayData() {
        dayRepo.deleteAll();
        dayRepo.addStores();
        dayRepo.addAdmins();
    }


    //TODO убрать этот говнокод
    public void updateStoreData(long userId){
        int store = dayRepo.findStoreByChat(userId);
        long storeChat = dayRepo.findChatStoreChatByStore(store);
        List<Day> everyUserData = dayRepo.findUsersByStore(store);
        Day allData = new Day();
        for (int i = 0; i < everyUserData.size(); i++) {
            allData.setHere(allData.getHere() + everyUserData.get(i).getHere());
            allData.setDelivery(allData.getDelivery() + everyUserData.get(i).getDelivery());
            allData.setMobile(allData.getMobile() + everyUserData.get(i).getMobile());
            allData.setEmail(allData.getEmail() + everyUserData.get(i).getEmail());
            allData.setFast(allData.getFast() + everyUserData.get(i).getFast());
            allData.setMobileCash(allData.getMobileCash() + everyUserData.get(i).getMobileCash());
        }
        dayRepo.updateAllHereByChat(allData.getHere(), storeChat);
        dayRepo.updateAllDeliveryByChat(allData.getDelivery(), storeChat);
        dayRepo.updateAllMobileByChat(allData.getMobile(), storeChat);
        dayRepo.updateAllEmailByChat(allData.getEmail(), storeChat);
        dayRepo.updateAllFastByChat(allData.getFast(), storeChat);
        dayRepo.updateAllMobileCashByChat(allData.getMobileCash(), storeChat);
    }

    public List<String> getTop(long chatId) {
        int store = dayRepo.findStoreByChat(chatId);
        StringBuilder builder = new StringBuilder();
        List<Day> hereTop = dayRepo.findHereTop(store);
        List<Day> deliveryTop = dayRepo.findDeliveryTop(store);
        List<Day> mobileTop = dayRepo.findMobileTop(store);
        List<Day> emailTop = dayRepo.findEmailTop(store);
        List<Day> fastTop = dayRepo.findFastTop(store);
        List<Day> mobileCashTop = dayRepo.findMobileCashTop(store);
        builder.append("<b>Топ по ЕК</b> \uD83D\uDDD1:\n");
        for (int i = 0; i < hereTop.size(); i++) {
            builder.append(i).append(" ").append(hereTop.get(i).getName()).append(" - ").append(hereTop.get(i).getHere()).append("\n");
        }
        builder.append("\n<b>Топ по доставкам</b> \uD83D\uDE9A: \n");
        for (int i = 0; i < deliveryTop.size(); i++) {
            builder.append(i).append(" ").append(deliveryTop.get(i).getName()).append(" - ").append(deliveryTop.get(i).getDelivery()).append("\n");
        }
        builder.append("\n<b>Топ по МП</b> \uD83D\uDCF1: \n");
        for (int i = 0; i < mobileTop.size(); i++) {
            builder.append(i).append(" ").append(mobileTop.get(i).getName()).append(" - ").append(mobileTop.get(i).getMobile()).append("\n");
        }
        builder.append("\n<b>Топ по МП Email</b> \uD83D\uDCE8: \n");
        for (int i = 0; i < emailTop.size(); i++) {
            builder.append(i).append(" ").append(emailTop.get(i).getName()).append(" - ").append(emailTop.get(i).getEmail()).append("\n");
        }
        builder.append("\n<b>Топ по быстрым продажам</b> \uD83D\uDCB8: \n");
        for (int i = 0; i < fastTop.size(); i++) {
            builder.append(i).append(" ").append(fastTop.get(i).getName()).append(" - ").append(fastTop.get(i).getFast()).append("\n");
        }
        builder.append("\n<b>Топ по Мобильной кассе</b> \uD83D\uDCB8: \n");
        for (int i = 0; i < mobileCashTop.size(); i++) {
            builder.append(i).append(" ").append(mobileCashTop.get(i).getName()).append(" - ").append(mobileCashTop.get(i).getMobileCash()).append("\n");
        }
        List<String> result = new ArrayList<>();
        result.add(builder.toString());
        return result;
    }

    public List<Day> getAllStores() {
        return dayRepo.findAllStores();
    }

    public List<Long> getAllUsersChat() {
        return dayRepo.findAllChats();
    }

    public List<Long> getSellersChats() {
        return dayRepo.findSellersChats();
    }

    public String getTodayStoreDataByChat(long userId) {
        int store = dayRepo.findStoreByChat(userId);
        Day data = dayRepo.findStoreByStore(store);
        StringBuilder builder = new StringBuilder();
        builder.append("Данные за сегодня ☀ ").append(data.getStore()).append("\n");
        builder.append("ЕК: ").append(data.getHere());
        builder.append("\nБП: ").append(data.getDelivery());
        builder.append("\nМП: ").append(data.getMobile());
        builder.append("\nEmail: ").append(data.getEmail());
        builder.append("\nБыстрые: ").append(data.getFast());
        builder.append("\nМК: ").append(data.getMobileCash());

        return builder.toString();
    }

    public List<Day> getAllAdmin() {
        return dayRepo.findAllAdmin();
    }

    public int getStoreByUserId(long userId) {
        return dayRepo.findStoreByChat(userId);
    }

    public String getSellersByAdminChat(long userId) {
        int store = dayRepo.findStoreByChat(userId);
        List<Day> listSellers = dayRepo.findSellersByStore(store);
        StringBuilder builder = new StringBuilder();
        builder.append("Добавленные продавцы:\n");
        for (int i = 0; i < listSellers.size(); i++) {
            builder.append(i).append(" - ").append(listSellers.get(i).getName()).append("\n");
        }
        return builder.toString();
    }

    public Day getPlanByStore(int store) {
        return dayRepo.findPlanByStore(store);
    }

    public void addPlanByStore(int store, String name) {
        dayRepo.addPlan(store, name);
    }

    public void setPlanByAdmin(long userId, String value, int sum) {
        int store = dayRepo.findStoreByChat(userId);
        switch (value){
            case "here" ->
                dayRepo.updateHerePlanByStore(sum, store);
            case "mobile" ->
                dayRepo.updateMobilePlanByStore(sum, store);
            case "mobile-cash" ->
                dayRepo.updateMobileCashPlanByStore(sum, store);
        }
    }

    public List<Day> getPlans() {
        return dayRepo.findPlans();
    }

    public List<Day> getSellersByStore(int store) {
        return dayRepo.findSellersByStore(store);
    }

    public Day getStoreByStore(int store) {
        return dayRepo.findStoreByStore(store);
    }

    public String getStoreDataByMain(int storeInt) {
        Day store = dayRepo.findStoreByStore(storeInt);
        Day plan = dayRepo.findPlanByStore(storeInt);
        List<Day> sellers = dayRepo.findSellersByStore(storeInt);
        int sellersPlan = 0;
        StringBuilder sellersPlanComplete = new StringBuilder();
        StringBuilder sellersPlanNotComplete = new StringBuilder();
        for (int i = 0; i < sellers.size(); i++) {
            boolean sellerPlanCheck = sellers.get(i).getHere() >= plan.getHere() && sellers.get(i).getMobile() >= plan.getMobile() && sellers.get(i).getMobileCash() >= plan.getMobileCash();
            if (sellerPlanCheck){
                sellersPlan = sellersPlan + 1;
                sellersPlanComplete.append(sellers.get(i).getName()).append(" ✅\n");
            } else {
                sellersPlanNotComplete.append(sellers.get(i).getName()).append(" ❌\n");
            }
        }
        String text = "<b>" + store.getName() + "\n" +
                "Данные сегодня ☀</b>\n" +
                "ЕК: " + store.getHere() + "/" + (plan.getHere() * sellers.size()) + "\n" +
                "ЕК БП: " + store.getDelivery() + "\n" +
                "МП: " + store.getMobile() + "/" + (plan.getMobile() * sellers.size()) + "\n" +
                "МП Email: " + store.getEmail() + "/" + (plan.getEmail() * sellers.size()) + "\n" +
                "МК: " + store.getMobileCash() + "/" + (plan.getMobileCash() * sellers.size()) + "\n" +
                "Быстрая продажа: " + store.getFast() + "\n\n" +
                "<b>Продавцы</b>\n" +
                "Всего " + sellers.size() + " продавцов\n" +
                "<b>План выполнили " + sellersPlan + " продавцов</b>\n" + sellersPlanComplete + "\n" +
                "<b>План не выполнили " + (sellers.size() - sellersPlan) + " продацов</b>\n" + sellersPlanNotComplete + "\n";
        return text;
    }
}
