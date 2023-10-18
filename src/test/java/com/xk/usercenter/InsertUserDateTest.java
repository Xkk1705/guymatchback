package com.xk.usercenter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.xk.usercenter.model.domain.User;
import com.xk.usercenter.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

@SpringBootTest
public class InsertUserDateTest {
    @Resource
    private UserService userService;

    @Test
    public void insetUserDateTest() {
        List<CompletableFuture<Void>> completableFutures = new ArrayList<>();

        User user = new User();
        user.setUsername("假dog");
        user.setUserAccount("fakedog");
        user.setAvatarUrl("https://cn.bing.com/images/search?view=detailV2&ccid=I11ThGcz&id=C96477AFDF394AEC0929FE249FE4060EEB267433&thid=OIP.I11ThGczcPRwTBV1oa2KMgAAAA&mediaurl=https%3a%2f%2ftupian.qqw21.com%2farticle%2fUploadPic%2f2020-8%2f20208522181014944.jpg&exph=450&expw=450&q=%e5%a4%b4%e5%83%8f%e5%9b%be%e7%89%87&simid=607986938147791464&FORM=IRPRST&ck=680ED87C40D12008C2AF593175DAAA17&selectedIndex=0");
        user.setGender(0);
        user.setUserPassword("12345678");
        user.setPhone("1895544648");
        user.setEmail("123@163.com");
        user.setUserStatus(0);
        user.setTags("[\"c++\",\"student\",\"java\",\"python\"]");
        user.setUserRole(0);
        user.setPlanetCode("111111");
        List<User> list = new ArrayList<>();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int j = 0;
        for (int i = 1; i <= 10; i++) {
                while (true){
                    j++;
                    list.add(user);
                    if (j % 1000 == 0) {
                        break;
                    }
                }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                userService.saveBatch(list, 1000);
                String name = Thread.currentThread().getName();
                System.out.println("当前线程Name："+ name);
            });
            completableFutures.add(future);
        }
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());


    }


    @Test
    public void insetUserDateTest1() {
        List<User> users = new ArrayList<>();
        User user = new User();
        user.setUsername("假dog");
        user.setUserAccount("fakedog");
        user.setAvatarUrl("https://cn.bing.com/images/search?view=detailV2&ccid=I11ThGcz&id=C96477AFDF394AEC0929FE249FE4060EEB267433&thid=OIP.I11ThGczcPRwTBV1oa2KMgAAAA&mediaurl=https%3a%2f%2ftupian.qqw21.com%2farticle%2fUploadPic%2f2020-8%2f20208522181014944.jpg&exph=450&expw=450&q=%e5%a4%b4%e5%83%8f%e5%9b%be%e7%89%87&simid=607986938147791464&FORM=IRPRST&ck=680ED87C40D12008C2AF593175DAAA17&selectedIndex=0");
        user.setGender(0);
        user.setUserPassword("12345678");
        user.setPhone("1895544648");
        user.setEmail("123@163.com");
        user.setUserStatus(0);
        user.setTags("[\"c++\",\"student\",\"java\",\"python\"]");
        user.setUserRole(0);
        user.setPlanetCode("111111");
        userService.save(user);

        for(int i = 0; i<10; i++) {
            users.add(user);
        }
        userService.saveBatch(users,10);


    }


}
