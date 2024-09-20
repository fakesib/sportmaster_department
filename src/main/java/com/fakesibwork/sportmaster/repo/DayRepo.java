package com.fakesibwork.sportmaster.repo;

import com.fakesibwork.sportmaster.model.Day;
import com.fakesibwork.sportmaster.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface DayRepo extends JpaRepository<Day, Integer> {

    @Modifying
    @Query(value = "DELETE FROM day", nativeQuery = true)
    void deleteAll();

    @Query(value = "SELECT store FROM day WHERE chat = ?1", nativeQuery = true)
    int findStoreByChat(long chat);

    @Query(value = "SELECT * FROM day WHERE store = ?1 AND role != 'STORE'", nativeQuery = true)
    List<Day> findUsersByStore(int store);

    @Modifying
    @Query(value = "DELETE FROM day WHERE id = ?1", nativeQuery = true)
    void deleteUserById(int id);

    @Query(value = "SELECT CASE WHEN EXISTS(SELECT FROM day WHERE chat = ?1) THEN 'true' ELSE 'false' END as user_exist", nativeQuery = true)
    boolean existsByChat(long userId);

    @Modifying
    @Query(value = "INSERT INTO day(id, chat, name, store, role, delivery, here, mobile, email, fast, mobile_cash) values(DEFAULT, ?1, ?2, ?3, ?4, 0, 0, 0, 0, 0, 0)", nativeQuery = true)
    void addUserByChat(long chat, String name, int store, String role);

    @Modifying
    @Query(value = "UPDATE day SET here = here + ?1 WHERE chat = ?2", nativeQuery = true)
    void updateHereByChat(int sum, long chat);

    @Modifying
    @Query(value = "UPDATE day SET delivery = delivery + ?1 WHERE chat = ?2", nativeQuery = true)
    void updateDeliveryByChat(int sum, long chat);

    @Modifying
    @Query(value = "UPDATE day SET mobile = mobile + ?1 WHERE chat = ?2", nativeQuery = true)
    void updateMobileByChat(int sum, long chat);

    @Modifying
    @Query(value = "UPDATE day SET email = email + ?1 WHERE chat = ?2", nativeQuery = true)
    void updateEmailByChat(int sum, long chat);

    @Modifying
    @Query(value = "UPDATE day SET fast = fast + ?1 WHERE chat = ?2", nativeQuery = true)
    void updateFastByChat(int sum, long chat);

    @Modifying
    @Query(value = "UPDATE day SET mobile_cash = mobile_cash + ?1 WHERE chat = ?2", nativeQuery = true)
    void updateMobileCashByChat(int sum, long chat);

    @Query(value = "SELECT * FROM day WHERE chat = ?1", nativeQuery = true)
    Day findUserByChat(long userId);

    @Modifying
    @Query(value = "UPDATE day SET here = here - ?1 WHERE chat = ?2", nativeQuery = true)
    void deleteHereByChat(int sum, long chat);

    @Modifying
    @Query(value = "UPDATE day SET delivery = delivery - ?1 WHERE chat = ?2", nativeQuery = true)
    void deleteDeliveryByChat(int sum, long chat);

    @Modifying
    @Query(value = "UPDATE day SET mobile = mobile - ?1 WHERE chat = ?2", nativeQuery = true)
    void deleteMobileByChat(int sum, long chat);

    @Modifying
    @Query(value = "UPDATE day SET email = email - ?1 WHERE chat = ?2", nativeQuery = true)
    void deleteEmailByChat(int sum, long chat);

    @Modifying
    @Query(value = "UPDATE day SET fast = fast - ?1 WHERE chat = ?2", nativeQuery = true)
    void deleteFastByChat(int sum, long chat);

    @Modifying
    @Query(value = "UPDATE day SET mobile_cash = mobile_cash - ?1 WHERE chat = ?2", nativeQuery = true)
    void deleteMobileCashByChat(int sum, long chat);

    @Query(value = "SELECT * FROM day WHERE role = 'STORE'", nativeQuery = true)
    List<Day> findAllStores();

    @Modifying
    @Query(value = "INSERT INTO day(store, chat, name, role, delivery, email, fast, here, mobile, mobile_cash) SELECT store, chat, name, role, 0 AS delivery, 0 AS mobile_cash, 0 AS email, 0 AS fast, 0 AS here, 0 AS mobile FROM users WHERE role = 'STORE'", nativeQuery = true)
    void addStores();

    @Modifying
    @Query(value = "INSERT INTO day(store, chat, name, role, delivery, email, fast, here, mobile, mobile_cash) SELECT store, chat, name, role, 0 AS delivery, 0 AS mobile_cash, 0 AS email, 0 AS fast, 0 AS here, 0 AS mobile FROM users WHERE role = 'ADMIN'", nativeQuery = true)
    void addAdmins();

    @Query(value = "SELECT chat FROM day WHERE store = ?1 AND role = 'STORE'", nativeQuery = true)
    long findChatStoreChatByStore(int store);

    @Modifying
    @Query(value = "UPDATE day SET here = ?1 WHERE chat = ?2", nativeQuery = true)
    void updateAllHereByChat(int sum, long chat);

    @Modifying
    @Query(value = "UPDATE day SET delivery = ?1 WHERE chat = ?2", nativeQuery = true)
    void updateAllDeliveryByChat(int sum, long chat);

    @Modifying
    @Query(value = "UPDATE day SET mobile = ?1 WHERE chat = ?2", nativeQuery = true)
    void updateAllMobileByChat(int sum, long chat);

    @Modifying
    @Query(value = "UPDATE day SET email = ?1 WHERE chat = ?2", nativeQuery = true)
    void updateAllEmailByChat(int sum, long chat);

    @Modifying
    @Query(value = "UPDATE day SET fast = ?1 WHERE chat = ?2", nativeQuery = true)
    void updateAllFastByChat(int sum, long chat);

    @Modifying
    @Query(value = "UPDATE day SET mobile_cash = ?1 WHERE chat = ?2", nativeQuery = true)
    void updateAllMobileCashByChat(int sum, long chat);

    @Query(value = "SELECT * FROM day WHERE store = ?1 AND here > 0 ORDER BY here DESC LIMIT 6", nativeQuery = true)
    List<Day> findHereTop(int store);

    @Query(value = "SELECT * FROM day WHERE store = ?1 AND delivery > 0 ORDER BY delivery DESC LIMIT 6", nativeQuery = true)
    List<Day> findDeliveryTop(int store);

    @Query(value = "SELECT * FROM day WHERE store = ?1 AND mobile > 0 ORDER BY mobile DESC LIMIT 6", nativeQuery = true)
    List<Day> findMobileTop(int store);

    @Query(value = "SELECT * FROM day WHERE store = ?1 AND email > 0 ORDER BY email DESC LIMIT 6", nativeQuery = true)
    List<Day> findEmailTop(int store);

    @Query(value = "SELECT * FROM day WHERE store = ?1 AND fast > 0 ORDER BY fast DESC LIMIT 6", nativeQuery = true)
    List<Day> findFastTop(int store);

    @Query(value = "SELECT * FROM day WHERE store = ?1 AND mobile_cash > 0 ORDER BY mobile_cash DESC LIMIT 6", nativeQuery = true)
    List<Day> findMobileCashTop(int store);

    @Query(value = "SELECT chat FROM day", nativeQuery = true)
    List<Long> findAllChats();

    @Query(value = "SELECT chat FROM day WHERE role = 'SELLER'", nativeQuery = true)
    List<Long> findSellersChats();

    @Query(value = "SELECT * FROM day WHERE store = ?1 AND role  = 'STORE'", nativeQuery = true)
    Day findStoreByStore(int store);

    @Query(value = "SELECT * FROM day WHERE role = 'ADMIN'", nativeQuery = true)
    List<Day> findAllAdmin();

    @Query(value = "SELECT * FROM day WHERE role = 'SELLER' AND store = ?1", nativeQuery = true)
    List<Day> findSellersByStore(int store);

    @Query(value = "SELECT * FROM day WHERE role = 'PLAN' AND store = ?1", nativeQuery = true)
    Day findPlanByStore(int store);

    @Modifying
    @Query(value = "INSERT INTO day(store, chat, name, role, delivery, email, fast, here, mobile, mobile_cash) values(?1, 0, ?2, 'PLAN', 0, 0, 0, 0, 0, 0)", nativeQuery = true)
    void addPlan(int store, String name);

    @Modifying
    @Query(value = "UPDATE day SET here = ?1 WHERE store = ?2 AND role = 'PLAN'", nativeQuery = true)
    void updateHerePlanByStore(int sum, int store);

    @Modifying
    @Query(value = "UPDATE day SET mobile = ?1 WHERE store = ?2 AND role = 'PLAN'", nativeQuery = true)
    void updateMobilePlanByStore(int sum, int store);

    @Modifying
    @Query(value = "UPDATE day SET mobile_cash = ?1 WHERE store = ?2 AND role = 'PLAN'", nativeQuery = true)
    void updateMobileCashPlanByStore(int sum, int store);

    @Query(value = "SELECT * FROM day WHERE role = 'PLAN'", nativeQuery = true)
    List<Day> findPlans();
}
