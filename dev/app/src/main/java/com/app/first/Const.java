package com.app.first;

public class Const {

    //download url
    static final String REQUEST_CONTENTS_LIST_URL= "https://qa-m.onestorebooks.co.kr/resources/nbook10/test/contentList.xml";

    //about DB
    static final String DB_FILE_NAME = "nBookData.db";
    static final String DB_TABLE_NAME = "content_table";
    static final int DB_VERSION = 1;

    //content download status  define
    static final int STATUS_ENABLE_DOWNLOAD = -1; //다운로드 가능
    static final int STATUS_FAIL_DOWNLOAD = -2;   //다운로드 실패
    static final int STATUS_COMPLETE_DOWNLOAD = 100; //다운로드 성공

    // B app Intent action & extra define
    static final String ACTION_GO_DOWNLOAD = "com.app.second.download";
    static final String EXTRA_KEY_FILE_NAME = "content_file_name";
    static final String EXTRA_KEY_FILE_DOWNLOAD_URL = "content_file_url";


}
