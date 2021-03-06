package com.wlgdo.avatar.service.users.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wlgdo.avatar.common.http.HttpResp;
import com.wlgdo.avatar.service.users.entity.Users;
import com.wlgdo.avatar.service.users.export.ExcelData;
import com.wlgdo.avatar.service.users.export.ExportExcelUtils;
import com.wlgdo.avatar.service.users.service.UsersService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dozer.DozerBeanMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author Ligang.Wang[wlgchun@163.com]
 * @since 2019-06-10
 */
@Slf4j
@AllArgsConstructor
@RestController
public class UsersController {

    private UsersService userService;

    /**
     * 该方法只是一个示例
     *
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @GetMapping("/users")
    public Object getUserList(@RequestParam Integer pageIndex, @RequestParam Integer pageSize) {

        IPage<Users> page = new Page<>(pageIndex, pageSize);
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        List<String> openIdAndUnionIdList = Lists.newArrayList("1-2", "2-2");
        queryWrapper.in("concat(open_id,'-',union_id)", openIdAndUnionIdList);
        queryWrapper.eq("open_id", 1);
        List<Users> list = userService.list(queryWrapper);
        log.info("result:{}", list);

//        IPage<Users> pageData = userService.page(page, queryWrapper);

        return HttpResp.instance().setData(list);
    }

    @GetMapping("/users/list")
    public Object getList(@RequestParam(required = false) String nickName, @RequestParam(required = false) String mobile) {

        QueryWrapper queryWrapper = new QueryWrapper<Users>();
        if (StringUtils.isNotBlank(nickName)) {
            queryWrapper.like("nick_name", nickName);
        }
        if (StringUtils.isNotBlank(mobile)) {
            queryWrapper.like("contact_number", mobile);
        }

        LambdaQueryWrapper<Users> lmbda = new LambdaQueryWrapper<>();
        lmbda.eq(Users::getOpenId, "openId");
        List<Users> list1 = userService.list(lmbda);

        List<Users> userlist = userService.list(queryWrapper);

        List<Users> list = userlist.stream().filter(e -> e.getSex() == 1).collect(Collectors.toList());

        List<String> openIds = list.stream().map(users -> users.getOpenId()).collect(Collectors.toList());

        List<Users> tacts = list.stream().map(users -> users).collect(Collectors.toList());

        DozerBeanMapper mapper = new DozerBeanMapper();

        List<Class<Users>> aList = list.stream().map(e -> Users.class).collect(Collectors.toList());

        List<Users> collect = list.stream().map(e -> new Users()).collect(Collectors.toList());

        Optional<Users> firstUser = list.stream().findFirst();
        Users actor = new Users();
        BeanUtils.copyProperties(actor, firstUser.get());

        return HttpResp.instance().setData(userlist);
    }

    /**
     * 导出excel数据
     *
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void excel(HttpServletResponse response) throws Exception {
        Long startTime = System.currentTimeMillis();
        QueryWrapper queryWrapper = new QueryWrapper<Users>();
        List<Users> userlist = userService.list(queryWrapper);
        ExcelData data = new ExcelData();
        data.setName("hello");
        List<String> titles = new ArrayList();
        titles.add("A");
        titles.add("B");
        titles.add("C");
        data.setTitles(titles);
        List<List<Object>> rows = new ArrayList();
        List<Object> row = null;
        for (Users u : userlist) {
            row = new ArrayList<>();
            row.add(u.getNickName());
            row.add(u.getOpenId());
            row.add(u.getPhone());
            rows.add(row);
        }
        data.setRows(rows);
        ExportExcelUtils.exportExcel(response, "TEST.xlsx", data);
        log.info("Total used time {}sm", (System.currentTimeMillis() - startTime) / 1000);
    }

    @RequestMapping(value = "/import", method = RequestMethod.GET)
    public void importExcel(@RequestParam("file") MultipartFile file) {
        try {
            ExportExcelUtils.importDataFromExcel(new Users(), file.getInputStream(), file.getOriginalFilename());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

