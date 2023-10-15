package com.xk.usercenter.service;
import java.util.Date;
import com.xk.usercenter.model.domain.User;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;


@SpringBootTest
class UserServiceTest {
    @Resource
    private UserService userService;

    @Test
    public void testAddUser() {
        User user = new User();
        user.setUsername("xk");
        user.setUserAccount("123");
        user.setAvatarUrl("https://cn.bing.com/images/search?view=detailV2&ccid=I11ThGcz&id=C96477AFDF394AEC0929FE249FE4060EEB267433&thid=OIP.I11ThGczcPRwTBV1oa2KMgAAAA&mediaurl=https%3a%2f%2ftupian.qqw21.com%2farticle%2fUploadPic%2f2020-8%2f20208522181014944.jpg&exph=450&expw=450&q=%e5%a4%b4%e5%83%8f%e5%9b%be%e7%89%87&simid=607986938147791464&FORM=IRPRST&ck=680ED87C40D12008C2AF593175DAAA17&selectedIndex=0");
        user.setGender(0);
        user.setUserPassword("123");
        user.setPhone("123");
        user.setEmail("123");
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        boolean save = userService.save(user);
        System.out.println(user.getId());
        Assert.assertTrue(save);


    }
}