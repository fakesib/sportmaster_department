package com.fakesibwork.sportmaster.service;

import com.fakesibwork.sportmaster.model.User;
import com.fakesibwork.sportmaster.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

//TODO План за месяц, выполнение плана


@Service
public class UserService {
    @Autowired
    private UserRepo userRepo;

    public String getRole(long userId){
        return userRepo.findRoleByChat(userId);
    }

    public String getUsersByStore(long userId) {
        int store = userRepo.findStoreByChat(userId);
        List<User> list = userRepo.findUsersByStore(store);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            builder.append(list.get(i).getId() + " - " + list.get(i).getName() + " - " + list.get(i).getRole() + "\n");
        }
        return "Сотрудники всего магазина:\nID - Имя Фамилия - Должность\n" + builder.toString();
    }

    public List<String> canDeleteUser(long userId, int deleteUserId){
        int store = userRepo.findStoreByChat(userId);
        List<User> users = userRepo.findUsersByStore(store);

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == deleteUserId && !users.get(i).getRole().equals("ADMIN") && !users.get(i).getRole().equals("DIRECTOR")){
                List<String> list = new ArrayList<>();
                list.add(String.valueOf(deleteUserId));
                list.add(users.get(i).getName());
                list.add(users.get(i).getRole());
                return list;
            }
        }
        return null;
    }

    public String deleteUser(List<String> strings) {
        userRepo.deleteById(Integer.parseInt(strings.get(1)));
        return "Пользователь " + strings.get(1) + " успешно удалён!";
    }

    public void addNewStaff(long userId, List<String> strings) {
        long chat = Long.parseLong(strings.get(2));
        String role = strings.get(1);
        String name = strings.get(3);
        int store = userRepo.findStoreByChat(userId);
        userRepo.addNewStaff(chat, role, store, name);
    }

    public User getUserById(String id) {
        return userRepo.findUserById(Integer.parseInt(id));
    }

    public String getMySales(long userId) {
        User user = userRepo.findUserByChat(userId);
        return "Ваши продажи за месяц:\n" +
                "ЕК: " + user.getHere() + "\n" +
                "БП: " + user.getDelivery() + "\n" +
                "МП: " + user.getMobile() + "\n" +
                "Email: " + user.getEmail() + "\n" +
                "<i>*Данные обновляются каждый вечер</i>";
    }

    public String updateDataByDay(List<Long> list){
        try {
            for (int i = 0; i < list.size(); i++) {
                userRepo.updateAllDayData(list.get(i));
            }
            return "Данные за месяц обновились";
        } catch (Exception e){
            return "exception of month data";
        }
    }

    public String getStoreDataByChat(long userId) {
        int store = userRepo.findStoreByChat(userId);
        User data = userRepo.findStoreByStore(store);
        StringBuilder builder = new StringBuilder();
        builder.append("Данные за месяц ").append(data.getStore()).append("\n");
        builder.append("ЕК: ").append(data.getHere());
        builder.append("\nБП: ").append(data.getDelivery());
        builder.append("\nМП: ").append(data.getMobile());
        builder.append("\nEmail: ").append(data.getEmail());
        builder.append("\nБыстрые: ").append(data.getFast());
        return builder.toString();
    }
}
