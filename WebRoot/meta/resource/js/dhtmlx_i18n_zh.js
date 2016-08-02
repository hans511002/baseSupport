/******************************************************
 *Copyrights @ 2011，Tianyuan DIC Information Co., Ltd.
 *All rights reserved.
 *
 *Filename：
 *      dhtmlx_i18n_zh.js
 *Description：
 *      dhtmlx中文显示信息。
 *Dependent：
 *       dhtmlx.js
 *Author:
 *        张伟
 *Finished：
 *       2011-09-11-9-14
 *Modified By：
 *
 * Modified Date:
 *
 * Modified Reasons:

 ********************************************************/

/**
 * 分页中文设置
 * @param pageSize
 */
dhtmlXGridObject.prototype.i18n.paging = {
    results:"结果集",
    records:"查询结果从 ",
    to:" 到 ",
    page:"页 ",
    perpage:"行/页",
    first:"第一页",
    previous:"前一页",
    found:"已找到记录",
    next:"下一页",
    last:"末页",
    of:" 有 ",
    notfound:"查询无结果"
};

/**
 * 定义验证失败的提示信息
 */
dhtmlxValidation.validateErrorMag = {
    isEmpty:"不能输入任何值！",
    isNotEmpty:"此项为必填！",
    isValidBoolean:"此项只能为布尔值",
    isValidEmail:"请输入正确的Email",
    isValidInteger:"请输入一个整数",
    isValidNumeric:"请输入一个数字",
    isValidAplhaNumeric:"只能输入字母和数字",
    isValidDatetime:"只能输入格式为YYYY-MM-DD HH-MI-SS的日期时间",
    isValidDate:"只能输入日期格式为YYYY-MM-DD的日期",
    isValidTime:"只能输入格式为HH-MI-SS的时间",
    isValidIPv4:"请输入正确的IP地址",
    isMin:"输入的最小值为{0}",
    isMax:"输入的最大值为{0}",
    isRange:"输入的值只能介于{0}到{1}之间",
    isMinLength:"输入的最小长度值为{0}",
    isMaxLength:"输入的最大长度值为{0}",
    isEqualTo:"请再次输入相同的值",
    isChinese:"只能输入中文",
    isPositiveInt:"只能输入正整数",
    isAlpha:"只能输入字母",
    isZip:"不是一个正确的邮政编码",
    isMobile:"不是一个正确的电话号码"
};
