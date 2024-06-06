package com.fakesibwork.sportmaster.repo;

import com.fakesibwork.sportmaster.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface UserRepo extends JpaRepository<User, Integer> {

    @Query(value = "SELECT role FROM users WHERE chat = ?1", nativeQuery = true)
    String findRoleByChat(long chat);

    @Query(value = "SELECT store FROM users WHERE chat = ?1", nativeQuery = true)
    int findStoreByChat(long chatId);

    @Query(value = "SELECT * FROM users WHERE store = ?1 ORDER BY name", nativeQuery = true)
    List<User> findUsersByStore(int store);

    @Modifying
    @Query(value = "DELETE FROM users WHERE id = ?1", nativeQuery = true)
    void deleteUserById(int id);

    @Modifying
    @Query(value = "INSERT INTO users(chat, role, store, name, here, delivery, mobile, email, fast, id) VALUES(?1, ?2, ?3, ?4, 0, 0, 0, 0, 0, DEFAULT)", nativeQuery = true)
    void addNewStaff(long chat, String role, int store, String name);

    @Query(value = "SELECT * FROM users WHERE id = ?1", nativeQuery = true)
    User findUserById(int id);

    @Query(value = "SELECT * FROM users WHERE chat = ?1", nativeQuery = true)
    User findUserByChat(long userId);

    @Modifying
    @Query(value = "UPDATE users SET delivery = delivery + (SELECT delivery FROM day WHERE day.chat = users.chat), " +
                                    "here = here + (SELECT here FROM day WHERE day.chat = users.chat), " +
                                    "mobile = mobile + (SELECT mobile FROM day WHERE day.chat = users.chat), " +
                                    "email = email + (SELECT email FROM day WHERE day.chat = users.chat), " +
                                    "fast = fast + (SELECT fast FROM day WHERE day.chat = users.chat) WHERE chat = ?1", nativeQuery = true)
    void updateAllDayData(long chat);

    @Query(value = "SELECT * FROM users WHERE store = ?1 AND role = 'STORE'", nativeQuery = true)
    User findStoreByStore(int store);

//    @Modifying
//    @Query(value = "UPDATE users SET here = ?1, delivery = ?2, mobile = ?3, email = ?4, fast = ?5 WHERE chat = ?6")
//    void updateData(int here, int delivery, int mobile, int email, int fast, long chat);
}
