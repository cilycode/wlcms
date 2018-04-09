package com.cily.wlcms.web;

import com.cily.wlcms.web.conf.SQLParam;
import com.cily.wlcms.web.controller.RoleController;
import com.cily.wlcms.web.controller.SysUserController;
import com.cily.wlcms.web.controller.UserController;
import com.cily.wlcms.web.interceptor.DeviceImeiInterceptor;
import com.cily.wlcms.web.interceptor.LogInterceptor;
import com.cily.wlcms.web.model.RightModel;
import com.cily.wlcms.web.model.RightRoleModel;
import com.cily.wlcms.web.model.RoleModel;
import com.cily.wlcms.web.model.UserModel;
import com.cily.utils.base.StrUtils;
import com.jfinal.config.*;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import com.jfinal.template.Engine;

/**
 * Created by admin on 2018/1/17.
 */
public class Conf extends JFinalConfig {
    @Override
    public void configConstant(Constants me) {
        PropKit.use("conf.properties");
    }

    @Override
    public void configRoute(Routes me) {
        me.add("user", UserController.class);
        me.add("sysUser", SysUserController.class);
        me.add("role", RoleController.class);
    }

    @Override
    public void configEngine(Engine me) {

    }

    @Override
    public void configPlugin(Plugins me) {
        C3p0Plugin c3p0 = new C3p0Plugin(PropKit.get("jdbcUrl"), PropKit.get("user"), PropKit.get("pwd"));
        me.add(c3p0);

        ActiveRecordPlugin arp = new ActiveRecordPlugin(c3p0);
        arp.setShowSql(PropKit.getBoolean("showSql"));
        me.add(arp);
        arp.addMapping(SQLParam.T_USER, SQLParam.USER_ID, UserModel.class);

        arp.addMapping(SQLParam.T_RIGHT, SQLParam.RIGHT_ID, RightModel.class);
        arp.addMapping(SQLParam.T_RIGHT_ROLE,
                StrUtils.join(SQLParam.RIGHT_ID, ",",
                        SQLParam.ROLE_ID), RightRoleModel.class);
        arp.addMapping(SQLParam.T_ROLE, SQLParam.ROLE_ID, RoleModel.class);
    }

    @Override
    public void configInterceptor(Interceptors me) {
        me.add(new LogInterceptor());
        me.add(new DeviceImeiInterceptor());
//        me.add(new LoginedInterceptor());
//        me.add(new RightInterceptor());
    }

    @Override
    public void configHandler(Handlers me) {

    }
}