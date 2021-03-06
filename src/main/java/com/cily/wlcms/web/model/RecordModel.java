package com.cily.wlcms.web.model;

import com.cily.utils.base.StrUtils;
import com.cily.utils.base.UUIDUtils;
import com.cily.utils.base.time.TimeUtils;
import com.cily.wlcms.web.conf.SQLParam;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;

import java.util.List;

/**
 * Created by admin on 2018/4/16.
 */
public class RecordModel extends Model<RecordModel> {
    private static RecordModel dao = new RecordModel();

    public static boolean insert(String recordName, String recordNum,
                                 String recordLevel, String recordContent,
                                 String recordImgUrl, String recordCreateUserId){
        if (StrUtils.isEmpty(recordName)){
            return false;
        }
        if (StrUtils.isEmpty(recordNum)){
            return false;
        }
        if (StrUtils.isEmpty(recordLevel)){
            return false;
        }
        if (StrUtils.isEmpty(recordCreateUserId)){
            return false;
        }

        RecordModel m = new RecordModel();
        m.set(SQLParam.RECORD_ID, UUIDUtils.getUUID());
        m.set(SQLParam.RECORD_NAME, recordName);
        m.set(SQLParam.RECORD_NUM, recordNum);
        m.set(SQLParam.RECORD_LEVEL, recordLevel);
        m.set(SQLParam.RECORD_CREATE_USER_ID, recordCreateUserId);
        m.set(SQLParam.CREATE_TIME, TimeUtils.milToStr(System.currentTimeMillis(), null));
        if (!StrUtils.isEmpty(recordContent)){
            m.set(SQLParam.RECORD_CONTENT, recordContent);
        }
        if (!StrUtils.isEmpty(recordImgUrl)){
            m.set(SQLParam.RECORD_IMG_URL, recordImgUrl);
        }
        return m.save();
    }

    public static long getEnableRecordCount(){
        return Db.queryLong(StrUtils.join("select count(1) from ",
                SQLParam.T_RECORD, " where ", SQLParam.RECORD_STATUS, " = '0'"));
    }
    public static long getDisenableRecordCount(){
        return Db.queryLong(StrUtils.join("select count(1) from ",
                SQLParam.T_RECORD, " where ", SQLParam.RECORD_STATUS, " = '1'"));
    }

    public static boolean updateRecord(String recordId, String recordName,
                                       String recordNum, String recordLevel,
                                       String recordContent, String recordImgUrl,
                                       String recordStatus){
        RecordModel m = getRecordById(recordId);
        if (m == null){
            return false;
        }
        if (!StrUtils.isEmpty(recordName)) {
            m.set(SQLParam.RECORD_NAME, recordName);
        }
        if (!StrUtils.isEmpty(recordNum)) {
            m.set(SQLParam.RECORD_NUM, recordNum);
        }
        if (!StrUtils.isEmpty(recordLevel)) {
            m.set(SQLParam.RECORD_LEVEL, recordLevel);
        }
//        if (!StrUtils.isEmpty(recordContent)){
//            m.set(SQLParam.RECORD_CONTENT, recordContent);
//        }
        if (recordContent != null){
            m.set(SQLParam.RECORD_CONTENT, recordContent);
        }
        if (!StrUtils.isEmpty(recordImgUrl)){
            m.set(SQLParam.RECORD_IMG_URL, recordImgUrl);
        }
        if (SQLParam.STATUS_ENABLE.equals(recordStatus)){
            m.set(SQLParam.RECORD_STATUS, recordStatus);
        }else {
            m.set(SQLParam.RECORD_STATUS, SQLParam.STATUS_DISABLE);
        }
        return m.update();
    }

    public static RecordModel getRecordById(String recordId){
        return dao.findById(recordId);
    }

    public static boolean delById(String recordId){
        return dao.deleteById(recordId);
    }

    public static boolean delByRecordNum(String recordNum){
        return Db.update(StrUtils.join("delete from ",
                SQLParam.T_RECORD, " where ", SQLParam.RECORD_NUM,
                " = '", recordNum, "';")) > 0;
    }

    public static Page<RecordModel> searchRecord(String searchText, String userId,
                                                 int pageNumber, int pageSize,
                                                 boolean onlySearchEnable){
        if (StrUtils.isEmpty(userId)) {
            //未查询创建者
//            return dao.paginate(pageNumber, pageSize, "select * ",
//                    StrUtils.join(" from ", SQLParam.T_RECORD));
            return dao.paginate(pageNumber, pageSize, StrUtils.join("select distinct ",
                    SQLParam.T_USER, ".", SQLParam.USER_NAME, ", ",
                    SQLParam.T_USER, ".", SQLParam.REAL_NAME, ", ", SQLParam.T_RECORD, ".* "),

                    StrUtils.join(" from ", SQLParam.T_RECORD, " left join ", SQLParam.T_USER, " on ",
                            SQLParam.T_USER, ".", SQLParam.USER_ID, " = ",
                            SQLParam.T_RECORD, ".", SQLParam.RECORD_CREATE_USER_ID,
                            " where (", SQLParam.RECORD_ID, " like '%", searchText, "%' or ",
                            SQLParam.RECORD_NUM, " like '%", searchText, "%' or ",
                            SQLParam.RECORD_NAME, " like '%", searchText, "%' or ",
                            SQLParam.RECORD_CONTENT, " like '%", searchText, "%')",
                            onlySearchEnable ? " and " + SQLParam.RECORD_STATUS + "='" + SQLParam.STATUS_ENABLE +"'": ""
                    ));
        }else {
            return dao.paginate(pageNumber, pageSize, StrUtils.join("select distinct ",
                    SQLParam.T_USER, ".", SQLParam.USER_NAME, ", ",
                    SQLParam.T_USER, ".", SQLParam.REAL_NAME, ", ", SQLParam.T_RECORD, ".* "),
                    StrUtils.join(" from ", SQLParam.T_RECORD, " left join ", SQLParam.T_USER, " on ",
                            SQLParam.T_USER, ".", SQLParam.USER_ID, " = ",
                            SQLParam.T_RECORD, ".", SQLParam.RECORD_CREATE_USER_ID,
                            " where ", SQLParam.USER_ID, " = ", userId, " and (",
                            SQLParam.RECORD_ID, " like '%", searchText, "%' or ",
                            SQLParam.RECORD_NUM, " like '%", searchText, "%' or ",
                            SQLParam.RECORD_NAME, " like '%", searchText, "%' or ",
                            SQLParam.RECORD_CONTENT, " like '%", searchText, "%')",
                            onlySearchEnable ? " and " + SQLParam.RECORD_STATUS + "='" + SQLParam.STATUS_ENABLE +"'": ""
                    ));
        }
    }

    public static Page<RecordModel> getRecordsByUserId(int pageNumber, int pageSize, String userId,
                                                       String recordStatus, boolean onlyLoadSystemRecord) {
        if (pageNumber < 1) {
            pageNumber = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        if (StrUtils.isEmpty(userId)) {
//            return dao.paginate(pageNumber, pageSize, "select * ",
//                    StrUtils.join(" from ", SQLParam.T_RECORD));
            if (SQLParam.STATUS_ENABLE.equals(recordStatus) || SQLParam.STATUS_DISABLE.equals(recordStatus)){
                return dao.paginate(pageNumber, pageSize, StrUtils.join("select distinct ",
                        SQLParam.T_USER, ".", SQLParam.USER_NAME, ", ",
                        SQLParam.T_USER, ".", SQLParam.REAL_NAME, ", ", SQLParam.T_RECORD, ".* "),
                        StrUtils.join(" from ", SQLParam.T_RECORD, " left join ", SQLParam.T_USER, " on ",
                                SQLParam.T_USER, ".", SQLParam.USER_ID, " = ", SQLParam.T_RECORD, ".", SQLParam.RECORD_CREATE_USER_ID,
                                " where ", SQLParam.T_RECORD, ".", SQLParam.RECORD_STATUS, " = ", recordStatus,
                                onlyLoadSystemRecord ? StrUtils.join(" and ", SQLParam.T_RECORD, ".", SQLParam.RECORD_CREATE_USER_ID, " = '", PropKit.get("rootUserId", "670b14728ad9902aecba32e22fa4f6bd"), "'") : "",
                                " order by ", SQLParam.T_RECORD, ".", SQLParam.UPDATE_TIME, " desc"));
            }else {
                return dao.paginate(pageNumber, pageSize, StrUtils.join("select distinct ",
                        SQLParam.T_USER, ".", SQLParam.USER_NAME, ", ",
                        SQLParam.T_USER, ".", SQLParam.REAL_NAME, ", ", SQLParam.T_RECORD, ".* "),
                        StrUtils.join(" from ", SQLParam.T_RECORD, " left join ", SQLParam.T_USER, " on ",
                                SQLParam.T_USER, ".", SQLParam.USER_ID, " = ", SQLParam.T_RECORD, ".", SQLParam.RECORD_CREATE_USER_ID,
                                " order by ", SQLParam.T_RECORD, ".", SQLParam.UPDATE_TIME, " desc"
                        ));
            }
        } else {
            if (SQLParam.STATUS_ENABLE.equals(recordStatus) || SQLParam.STATUS_DISABLE.equals(recordStatus)){
                return dao.paginate(pageNumber, pageSize, StrUtils.join("select distinct ",
                        SQLParam.T_USER, ".", SQLParam.USER_NAME, ", ",
                        SQLParam.T_USER, ".", SQLParam.REAL_NAME, ", ", SQLParam.T_RECORD, ".* "),
                        StrUtils.join(" from ", SQLParam.T_RECORD, " left join ", SQLParam.T_USER, " on ",
                                SQLParam.T_USER, ".", SQLParam.USER_ID, " = ", SQLParam.T_RECORD, ".", SQLParam.RECORD_CREATE_USER_ID,
                                " where ", SQLParam.T_RECORD, ".", SQLParam.RECORD_CREATE_USER_ID, " = '", userId, "' and ",
                                SQLParam.T_RECORD, ".", SQLParam.RECORD_STATUS, " = ", recordStatus, " order by ", SQLParam.T_RECORD, ".", SQLParam.UPDATE_TIME, " desc"
                        ));
            }else {
                return dao.paginate(pageNumber, pageSize, StrUtils.join("select distinct ",
                        SQLParam.T_USER, ".", SQLParam.USER_NAME, ", ",
                        SQLParam.T_USER, ".", SQLParam.REAL_NAME, ", ", SQLParam.T_RECORD, ".* "),
                        StrUtils.join(" from ", SQLParam.T_RECORD, " left join ", SQLParam.T_USER, " on ",
                                SQLParam.T_USER, ".", SQLParam.USER_ID, " = ", SQLParam.T_RECORD, ".", SQLParam.RECORD_CREATE_USER_ID,
                                " where ", SQLParam.T_RECORD, ".", SQLParam.RECORD_CREATE_USER_ID, " = '", userId, "'",
                                " order by ", SQLParam.T_RECORD, ".", SQLParam.UPDATE_TIME, " desc"
                        ));
            }
        }
    }

//    public static Page<RecordModel> getRecordsWithComment(int pageNumber, int pageSize){
//
//    }
}
