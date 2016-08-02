/******************************************************
 *Copyrights @ 2011，Tianyuan DIC Information Co., Ltd.
 *All rights reserved.
 *
 *Filename：
 *        dhtmlxExtend.js
 *Description：
 *        此JS主要用于对DHTMLX功能的的扩展以及提供一些基本工具函数，这些工具函数的命名空间为Tools.并且提供了针对dwr访问的的控件
 *biDwrMethodParam 和 biDwrCaller控件用于与DHTMX控件的集成。
 *Dependent：
 *        Dwr 的JS文件，如util.js和engine.js 以及dhmtlx.js
 *Author:
 *        张伟
 *Finished：
 *       2011-09-2011-9-5
 *Modified By：
 *
 * Modified Date:
 *
 * Modified Reasons:

 ********************************************************/

/***************************************************************************
 *                     Tools工具类定义
 ***************************************************************************/


var Tools = new Object();//工具函数命名空间
/**
 * 跨浏览器添加事件
 * @param oTarget 要添加事件的DOM节点
 * @param sEventType  事件名称，如“click”
 * @param funName 事件函数
 */
Tools.addEvent = function(oTarget, sEventType, funName){
    if(oTarget.addEventListener){//for DOM;
        oTarget.addEventListener(sEventType, funName, false);
    } else if(oTarget.attachEvent){
        oTarget.attachEvent("on" + sEventType, funName);
    } else{
        oTarget["on" + sEventType] = funName;
    }
};
/**
 * 垮浏览器为制定节点移除事件
 * @param oTarget 要添加事件的DOM节点
 * @param sEventType  事件名称，如“click”
 * @param funName 事件函数
 */
Tools.removeEvent = function(oTarget, sEventType, funName){
    if(oTarget.removeEventListener){//for DOM;
        oTarget.removeEventListener(sEventType, funName, false);
    } else if(oTarget.detachEvent){
        oTarget.detachEvent("on" + sEventType, funName);
    } else{
        oTarget["on" + sEventType] = null;
    }
};

/**
 * 产生一个随机的不重复的字符串。
 * @param w
 */
Tools.genStr = function(w){
    var s = "";
    var z = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    for(var q = 0; q < w; q++){
        s += z.charAt(Math.round(Math.random() * (z.length - 1)));
    }
    return s;
};

/**
 * 去除字符串左右空格
 * @param s
 */
Tools.trim = function(s){
    return s.replace(/(^\s*)|(\s*$)/g, "");
}
/**
 * 去除字符串左空格
 * @param s
 */
Tools.ltrim = function(s){
    return s.replace(/(^\s*)/g, "");
}
/**
 * 去除字符串右空格
 * @param s
 */
Tools.rtrim = function(s){
    return s.replace(/(\s*$)/g, "");
}
/**
 * 判断指定对象是否为空对象，即{}形式
 * @param obj
 * @return 当不是对象类型时，返回false，当为null undefined 返回false
 */
Tools.isEmptyObject = function(obj){
    if(typeof obj == "object"){
        var cont = 0;
        for(var key in obj){
            cont++
        }
        return !cont;
    }
    return !!obj;
}
/***************************************************************************
 *                     biDwrMethodParam组件定义
 ***************************************************************************/

/**
 * 此组件封装DWR的方法参数，主要描述参数的个数，参数的获取方式等等。
 * 注：此组件与biDwrcaller联合使用
 * 此组件使用方式：
 * 初始化方式两种：  一:使用构造函数 var param=new biDwrMethodParam([{index:0,type:"ui",vaule:"input1,inut2"}
 * ,{..}..])
 *                 二:使用实例化函数 var param=biDwrMethodParam.instance([{index:0,type:"ui",vaule:"input1,inut2"}
 * ,{..}..])
 *  初始化之后，可用方法setParamConfig设置参数配置，相见setParamConfig 方法注释。
 *  也可以用 setParam 进行参数设置。
 *  此控件也可以作为URl参数中的一个与bidwrCaller控件联合使用
 *  例： var testParam= new biDwrMethodParam();
 testParam.setParamConfig([{
 index:0,type:"ui",value:"_serach,id"
 },{
 index:1,type:"fun",value:function(){return {start:1}}
 }]);
 var dwrtest=new biDwrCaller();
 dwrtest.loadAction("UserCtrlr.dhtmlXuserList");
 var url=dwrtest+"?runAction=load&"+"param="+testParam;
 grid.load(url,"json");
 * @param config 详见setParamConfig方法。
 */
var biDwrMethodParam = function(config){
    this.size = 0; //参数个数
    this.paramConfig = [];//参数设置
    this._setParam = [];//用户自定义设置的param
    this._isBiDwrMethodParam = true;
    if(config){
        this.setParamConfig(config);
    }
    var uid = dhx.uid();
    biDwrMethodParam._runParam[uid] = this;
    //返回固定格式的字符串，偏于重新序列化对象。
    this.formatString = "&_biDwrMethodParam=_###biDwrMethodParam_Object_uid:" + uid;
    this.length = this.formatString.length;
};
/**
 * 设置一个静态变量，用于存储调用了biDwrMethodParam toString方法的 biDwrCaller引用，便于后续恢复对象。
 */
biDwrMethodParam._runParam = [];
/**
 * 当此对象序列化成一个String字符串，偏于进行字符串的连接，即此对象可以类似于URL一般
 * 可以用"+”操作符进行参数的连接，连接后的字符串只要用dhx.ajax()调用，自然会还原成原来的对象
 * 实现了本控件与传统URL的无缝连接。
 */
biDwrMethodParam.prototype.toString = function(){
    return this.formatString;
}
biDwrMethodParam.prototype.indexOf = function(s, position){
    return this.formatString.indexOf(s, position);
}
/**
 * 将URL格式字符串字符串转换为param对象。
 * 设计为静态方法
 * @param s
 * @return object biDwrMethodParam对象
 */
biDwrMethodParam.format = function(s){
    var uid = null;
    if(uid = biDwrMethodParam.isDwrMethodParamObject(s)){
        //获取biDwrMethodParam
        return biDwrMethodParam._runParam[uid];
    }
}

/**
 * 静态方法判断指定字符串是否是由biDwrMethodParam对象生成。如果是返回其对象uid，否则为false;
 * @param s
 */
biDwrMethodParam.isDwrMethodParamObject = function(s){
    if(typeof s == "object"){
        s = s.formatString;
    }
    if(s && typeof s == "string"){
        var reg = /^&_biDwrMethodParam=_#{3}biDwrMethodParam_Object_uid:(\d+)/;
        var match = null;
        return s && (match = reg.exec(s)) ? match[1] : false;
    }
    return false;
}
/**
 * 参数合并
 * @param val1
 * @param val2
 */
biDwrMethodParam.connectParam = function(val1, val2){
    if(val1 == null || val1 == undefined){
        return val2;
    }
    if(val2 == null || val2 == undefined){
        return val1;
    }
    var type1 = typeof val1;
    var type2 = typeof val2;
    if(type1 != "object" && type2 != "object"){//两个都不是对象，连接返回一个数组,对应Java数组类型
        return [type1,type2];
    } else if(type1 == "object"){ //如果type1是对象
        if(dhx.isArray(val1)){ //如果val1是数组类型，连接成为数组
            if(dhx.isArray(val2)){//value2也是数组，直接合并
                val1.concat(val2);
                return val1;
            }
            return val1.push(val2) && val1;//其他原始直接添加。
        } else{//如果value1不是数组，是个纯粹的对象
            if(type2 != "object"){//如果type2不是对象，是string或者是numbei，不能连接
                return null;
            } else{//如果type2是对象
                if(dhx.isArray(val2)){//如果是数组，则value1加入数组中
                    return val2.push(val1) && val2;
                } else{//是对象直接合并
                    return dhx.extend(val1, val2, true);
                }
            }
        }
    } else{//如果value1不是对象
        return biDwrMethodParam.connectParam(val2, val1);
    }
};

/**
 * 实例化一个biDwrMethodParam
 * @param config config为JS数组,配置见setParamConfig方法。
 */
biDwrMethodParam.instance = function(config){
    return new biDwrMethodParam(config);
}
/**新增参数配置
 * @author 张伟
 * @param config config为JS数组，完整配置定义示例如下：
 * var config=[
 * {index:0, dwr方法索引位置,必须。
 *  type:"ui", 参数来源，有四种，"ui"：参数来源于页面上组件集合，多个组件以","号分隔，如果其中有form组件，则会遍历form下所有的元素取值
 *                             "fun":用户自定义回调函数取参数值，取其返回结果。
 *                             "static":静态常量值，默认，如果是这种类型type可以省略。
 *                             "watch":动态观察某一个函数的调用，当某一个函数被调用时，会调用一个用户自定义的回调函数返回参数。
 *  vaule:fuction(){return "a"};  必须，参数值，type="ui"时，为ui字符串，如"input1,input2"
 *                                            type="fun"时，为用户自定义回调函数。
 *                                           type="static",为参数实际值
 *                                            type=”watch“时，为动态监测的函数名或者对象，函数名，如”fun1“，如果为某一对象下的函数名
 * 组合为数组，如obj.fun1组合为[obj,"fun1"]，注：函数必须提供函数名，否者无效。
 * append:true/false 对同一索引的参数，如果之前有设置，这里是否进行参数合并也即追加，默认为true，可选。为false将代替前面的同样索引位的参数设置。
 * 参数合并规则如下：
 * 1、当计算出参数都为数组时，进行数组合并，如[1,2]&&[3,4]=[1,2,3,4]
 * 2、当地一个参数为数组时，第二个参数为非数组的其他对象，同样合并为数组，如[1,2]&&2=[1,2,2]或者[1,2]&&{a:1}=[1,2,{a:1}]
 * 3、当第一个参数为对象时，第二个参数为非对象时，不能合并，如{a:1}&&1==error!
 * 4、当第一个参数为对象时，第二个参数为对象时，对象继承，如{a:1}&&{a:2,b:3}={a:2,b:3}
 * 其他依次类推。
 * watchCallback:当type为”watch“时，此参数为必须，监测回调函数，此回调函数有两个参数，第一个为对应函数的调用参数，第二个为函数的返回参数。
 * 例：function(first,second){
 *    return 。。。//返回的极为当时的参数，如果调用多次以最后一次参数生效。
 * }
 * ,{。。。。}
 * ]
 */
biDwrMethodParam.prototype.setParamConfig = function(config){
    var methodSize = this.size;
    if(!dhx.isArray(config)){
        alert("参数定义错误！");
        return;
    }
    for(var i = 0; i < config.length; i++){
        var temp = config[i];
        methodSize = methodSize <= temp.index ? temp.index + 1 : methodSize;
        temp.uid = dhx.uid();
        this.paramConfig[temp.index] = this.paramConfig[temp.index] || [];
        if(dhx.isNotDefined(temp.append) || temp.append == null || temp.append){//追加配置
            this.paramConfig[temp.index].push(config[i]);
        } else{//否则替换。
            for(var ii = 0; ii < this.paramConfig[temp.index].length; i++){
                if(this.paramConfig[temp.index][ii]._deleteWatch){
                    this.paramConfig[temp.index][ii]._deleteWatch(this.paramConfig[temp.index][ii]);
                }
                this.paramConfig[temp.index][ii] = null;
            }
            this.paramConfig[temp.index] = [(config[i])]
        }
        if(config[i].type == "watch"){//如果是监测某一个函数。
            var orgFun = null;
            var funName = null;
            var obj = window;
            if(typeof config[i].value == "string"){
                funName = config[i].value;
                orgFun = window[config[i].value];
            } else if(dhx.isArray(config[i].value)){
                if(config[i].value.length == 1){
                    funName = config[i].value[0];
                    orgFun = window[config[i].value[0]];
                } else{
                    funName = config[i].value[1];
                    obj = config[i].value[0];
                    orgFun = obj[funName];
                }
            }
            if(!orgFun){
                alert("对应索引位" + temp.index + "未找到所要监测的函数！");
                continue;
            } else if(typeof orgFun != "function"){
                alert("对应索引位" + temp.index + "配置中的监测不是一个函数！")
                continue;
            } else if(!temp.watchCallback){
                alert("对应索引位" + temp.index + "未定义监测回调函数！")
                continue;
            }
            //替换监测的函数。
            var that = this;
            if(orgFun._dwrwatch){
                temp._addWatch(temp);
            } else{
                obj[funName] = function(){
                    var args = arguments.length > 0 ? Array.prototype.slice.call(arguments) : undefined;
                    var result = obj[funName]._orgFun.apply(obj, arguments);
                    temp._addWatch = function(param, config){
                        obj[funName]._dwrwatch.push({
                                                        param:param,
                                                        config:config
                                                    });
                    };
                    temp._deleteWatch = function(config){
                        for(var key = 0; key < obj[funName]._dwrwatch.length; key++){
                            if(config.uid == obj[funName]._dwrwatch[key].config.uid){
                                //删除原来记的watch变量值
                                delete obj[funName]._dwrwatch[key].param._watch[obj[funName]._dwrwatch[key].config.uid];
                                obj[funName]._dwrwatch[key].param = null;
                                obj[funName]._dwrwatch[key].config = null;
                                delete obj[funName]._dwrwatch[key];
                            }
                        }
                        if(obj[funName]._dwrwatch.length == 0){
                            obj[funName]._dwrwatch = null;
                            obj[funName] = obj[funName]._orgFun;
                        }
                    };
                    //依次调用监听的回调函数。
                    for(var key = 0; key < obj[funName]._dwrwatch.length; key++){
                        var tmppp = obj[funName]._dwrwatch[key];
                        tmppp.param._watch = tmppp.param._watch || [];
                        tmppp.param._watch[tmppp.config.index] = tmppp.param._watch[tmppp.config.index] || [];
                        tmppp.param._watch[tmppp.config.index][tmppp.config.uid] = tmppp.config.watchCallback(args,
                                                                                                              result);
                    }
                };
                obj[funName]._orgFun = orgFun;
                obj[funName]._dwrwatch = [
                    {
                        param:that,
                        config:temp
                    }
                ];
            }
        }
    }
    this.size = methodSize;
    return this;
};
/**
 * 根据参数配置生成参数。
 */
biDwrMethodParam.prototype.buildParam = function(){
    var arg = [];
    arg[this.size - 1] = null;
    for(var i = 0; i < this.size; i++){
        var configs = this.paramConfig[i];
        if(configs){
            for(var k = 0; k < configs.length; k++){
                var config = configs[k];
                if(config){
                    var value = config.value;
                    switch(config.type){
                        case "ui":
                        {
                            value = value.split(",");
                            var uiParam = {};
                            for(var j = 0; j < value.length; j++){
                                var node = dwr.util.byId(value[j]);
                                if(!node){
                                    var nodes = document.getElementsByName(value[j]);
                                    if(nodes.length >= 1){
                                        node = nodes.item(0);
                                    }
                                }
                                if(!node){
                                    continue;
                                }
                                var name = node.name;
                                //                                if(!name) continue;
                                if(node.nodeName.toLowerCase() == "form"){//form 表单
                                    var formParam = dwr.util.getFormValues(value[j]);
                                    if(formParam){
                                        for(var z in formParam){
                                            if(uiParam[z]){
                                                uiParam[z] = dhx.isArray(uiParam[z]) ? uiParam[z] : [uiParam[z]];
                                                uiParam[z].push(formParam[z]);
                                            } else{
                                                uiParam[z] = formParam[z];
                                            }
                                        }
                                    }
                                } else{
                                    var fieldValue = dwr.util.getValue(value[j]);
                                    if(fieldValue){
                                        if(uiParam[name]){
                                            uiParam[name] = dhx.isArray(uiParam[name]) ? uiParam[name] : [uiParam[name]
                                            ];
                                            uiParam[name].push(fieldValue);

                                        } else{
                                            uiParam[name] = fieldValue;
                                        }
                                    }
                                }
                            }
                            arg[i] = biDwrMethodParam.connectParam(arg[i], uiParam);
                            break;
                        }
                        case "fun":
                        {//用户自定义回调函数获取参数
                            arg[i] = biDwrMethodParam.connectParam(arg[i], value());
                            break;
                        }
                        case "static":
                        {
                            arg[i] = biDwrMethodParam.connectParam(arg[i], value);
                            break;
                        }
                        case "watch":
                        {
                            if(this._watch && this._watch[i] && this._watch[i][config.uid]){
                                arg[i] = biDwrMethodParam.connectParam(arg[i], this._watch[i][config.uid]);
                            }
                            break;
                        }
                        default:
                            break;
                    }
                } else{
                    arg[i] = biDwrMethodParam.connectParam(arg[i], null);
                }
            }
        }
        arg[i] = biDwrMethodParam.connectParam(arg[i], this._setParam[i]);
    }
    return arg;
};
/**
 * 格式类似URL的字符串，比如"www.sina.com?id=5&name=zzz"，解析此字符串，获取其URL参数
 * 组装为一个JS对象。
 * @param url
 */
biDwrMethodParam.urlParamFormat = function(url){
    var reg = /[\?|&]{1}([^&|\?|\s|=]+)={1}([^&|\?|\s]*)/g;
    var tempParam = null;
    var append = null;
    while(tempParam = reg.exec(url)){
        var key = tempParam[1];
        var value = decodeURIComponent(tempParam[2]);
        append = append || {};
        //如果URL中有前后中括号的形式，例dhx_filter[name]=value;解析成一个HASH形式，即
        /*    {dhx_filter:{name:value}}形式*/
        var reg1 = /([^\[|\]]+)\[{1}([^\[|\]]+)\]{1}/;
        var filter = reg1.exec(key);
        if(filter){
            key = filter[1];
            var empty = {};
            empty[filter[2]] = value;
            if(append[key]){ //对象扩展
                append[key] = dhx.extend(append[key], empty, true);
            } else{
                append[key] = empty;
            }
            continue;
        }
        if(append[key]){ //扩展为链表
            var t = append[key];
            if(dhx.isArray(append[key])){
                append[key].push(value);
            } else{
                append[key] = [];
                append[key].push(t);
                append[key].push(value);
            }
        } else{
            append[key] = value;
        }
    }
    reg.lastIndex = 0;
    return append;
}

/**
 * 设置静态常量参数
 * @param index
 * @param value
 * @param append 是否与前面相同索引的设置进行追加。默认为true
 */
biDwrMethodParam.prototype.setParam = function(index, value, append){
    this.size = this.size <= index ? index + 1 : this.size;
    if(dhx.isNotDefined(append) || append == null || append){
        this._setParam[index] = biDwrMethodParam.connectParam(this._setParam[index], value);
    } else{
        this._setParam[index] = value;
    }
}

/**
 *
 * 移除索引位所有的配置。
 * @param indexs 索引序列,数组
 */
biDwrMethodParam.prototype.clearConfigByIndex = function(index){
    if(index > this.size - 1){
        return;
    }
    this._setParam[index] = null;
    this._watch[index] = null;
    this.paramConfig[index] = null;
    if(this.size - 1 == index){
        this.size--;
    }
}


/***************************************************************************
 *                     biDwrCaller组件定义
 ***************************************************************************/


/**
 * dwr封装,此控件完全可以作为URL传递。
 * 当此控件示例为：
 *  1、使用方法一：使用方法添加Action
 *  var dwr=new biDwrCaller(); //声明一个dwr控件
 *  dwr.loadAction("UserCtrlr.dhtmlXuserList",null);//添加loadAction
 *  var url=dwr+'?runAction=load';//runAction代表此次URL要执行的Action，默认为"load"
 *  还可以给dwr执行时添加额外参数，这里的参数将作为HSAH方式传入DWR方法最后一个 例
 *  url = url+(url.indexOf("?")==-1?"?":"&")+"action=get&id="+encodeURIComponent("asasa")
 *  当用做URl时，只能适用于DHTMX控件
 *  比如：grid.load(url,"json"); 此Url如上所示；
 *  2、使用方法二：构造函数直接初始化
 *  var dwr=new biDwrCaller({
 *                           load:function(afterCall,param){...}//定义一个完全自控的Action
 *                           update:{methodName:'UserCtrl.logion',param:[null,null],sucess:function(data){}}//定义一个交由
 *                   控件处理的Action..
 })
 */
var biDwrCaller = function(config){
    this._action = [];
    //先将原有对象保留
    var uid = dhx.uid();
    biDwrCaller._runCaller[uid] = this;
    //返回固定格式的字符串，偏于重新序列化对象。
    this.formatString = "_###biDwrCaller_Object_uid:" + uid;
    this.dwr = {};
    this.length = this.formatString.length;
    //寻找当前页的dwr ctrl
    var reg = /^(\/\S+)*\/dwr\/interface\/(\w+)\.js/;
    var scripts = document.querySelectorAll ? document.querySelectorAll("script[src]") : document
            .getElementsByTagName("script");
    for(var i = 0; i < scripts.length; i++){
        var src = scripts[i].getAttribute("src");
        if(!src){
            continue;
        }
        var match = reg.exec(src);
        if(match){
            dhx.extend(this.dwr, window[match[2]]); //依次继承Dwr中的方法。
        }
    }
    if(config){//如果有初始化配置
        for(var key in config){
            if(typeof config[key] == "function"){//如果是个function
                this.addAction(key, config[key]);
            } else if(typeof config[key] == "object"){
                var param = config[key].param ? (dhx.isArray(config[key].param) ? config[key].param : [config[key].param
                ]) : [];
                var execute = "this.addAutoAction(key,config[key].methodName,";
                for(var i = 0; i < param.length; i++){
                    execute += "param[" + i + "],"
                }
                execute += "config[key].success)";
                eval(execute);
            }
        }
    }
}
/**
 * 设置一个静态变量，用于存储调用了biDwrCaller toString方法的 biDwrCaller引用，便于后续恢复对象。
 */
biDwrCaller._runCaller = [];
/**
 * 当此对象序列化成一个String字符串，偏于进行字符串的连接，即此对象可以类似于URL一般
 * 可以用"+”操作符进行参数的连接，连接后的字符串只要用dhx.ajax()调用，自然会还原成原来的对象
 * 并且会组装后面的URL参数（注：不管后面的参数有多少个，依次按照key=value格式组装成hash数据结构传递给
 * dwr方法的最后一个参数），传递给DWR调用，实现了DWR与传统URl调用的无缝整合。
 */
biDwrCaller.prototype.toString = function(){
    return this.formatString;
}
biDwrCaller.prototype.indexOf = function(s, position){
    return this.formatString.indexOf(s, position);
}

/**
 * 将URL格式字符串字符串转换为DwR对象，并自动处理链接在其后以URL格式的参数，如果此字符串不是由DWR序列而成，返回Null。
 * 设计为静态方法
 * @param s
 * @return object 包含三个属性：dwrCaller:对应dwrCaller对象，appendParam:追加的param参数，runAction:此URL代表要执行的Action
 */
biDwrCaller.format = function(url){
    var uid = null;
    if(uid = biDwrCaller.isDwrCallerObject(url)){
        var reg = /^_#{3}biDwrCaller_Object_uid:\d+(([\?|&]{1}[^&|\?|\s|=]+={1}[^&|\?|\s]*)*)/;
        var temp = reg.exec(url);
        var append = null;
        var totalParam = [];
        var runAction = null;
        if(temp && temp.length > 1 && temp[1]){
            var param = temp[1];
            append = biDwrMethodParam.urlParamFormat(param);
            for(var key in append){
                if(key == "runAction"){
                    runAction = append[key];
                    delete append[key];
                    continue;
                }
                //判断是否是biDwrMethodParam控件
                if(biDwrMethodParam.isDwrMethodParamObject(append[key])){
                    totalParam.push(biDwrMethodParam.format(append[key]));
                    delete append[key];
                }
            }
        }
        append = Tools.isEmptyObject(append) ? null : append;
        reg.lastIndex = 0;
        //获取dwrCaller
        var dwrCaller = biDwrCaller._runCaller[uid];
        var result = new Object();
        result.dwrCaller = dwrCaller;
        if(totalParam.length > 0){
            if(!Tools.isEmptyObject(append)){
                totalParam.push(append);
            }
            result.appendParam = totalParam;
        } else if(!Tools.isEmptyObject(append)){
            result.appendParam = [append];
        }
        result.runAction = runAction || "load";
        return result;
    }
}
/**
 * 静态方法判断指定字符串是否是由biDwrCaller对象生成。如果是返回其对象uid，否则为false;
 * @param s
 */
biDwrCaller.isDwrCallerObject = function(s){
    if(typeof s == "object"){
        s = s.formatString;
    }
    if(s && typeof s == "string"){
        var reg = /^_#{3}biDwrCaller_Object_uid:(\d+)/
        var match = null;
        return s && (match = reg.exec(s)) ? match[1] : false;
    }
    return false;
}
/**
 * 新增一个自动访问action，该Action基于DWr封装，会自动调用对应的Dwr方法，会产生一个名称格式为"doActionXXX"的方法
 * @param actionName action名称，如“load”
 * @param methodName 执行的DWR方法名，以字符串表示，如果页面已经引入了对应的DWR JS文件，则直接可以省略DWR crtl类名。
 * 如"UserCtrlr.login 可以省略为login，注，如果页面上引入的DWR JS文件中有重复的方法名，则必须指定对应的ctrl名，如果
 * Dwr CtrlrJS文件未加载，第一次使用此方法需指明具体的ctrlr，如"UserCtrlr.login ",此方法会自动加载对应的DWR文件，以后
 * 使用即可省略ctrlr
 * @param dwrParam  dwr方法执行的参数，可省略，也可以多个，比如param1,param2.。。。等，也可以使用封装的dwr参数组件biDwrMethodParam
 * 此控件只能有一个，和普通参数不能共存，意思是说设置了biDwrMethodParam，程序就不会读后面的参数了。 如果和一些DHTMX联合使用，一些参数
 * 比如分页，比如tree动态加载的ID会由控件自动提供。
 * @param success  dwr回调函数，远程访问成功即调用此函数，可选，如存在回调，则回调函数必须置于此方法参数的最后一个，且必须是函数
 * 类型。若和DHTMLX联合使用，回调之后需要DHTMLX继续执行下面的动作，比如数据加载，需要返回data或者true，如果需要显示的定义程序就此终止，
 * 不在执行，需直接返回false（注：false之后，程序不会自动执行下面的动作，也可以主动调用afterCall()函数传入data继续执行后面的操作。
 * 如果存在afterCall()函数，将作为第二个参数传递给success回调函数。）
 */
biDwrCaller.prototype.addAutoAction = function(actionName, methodName, dwrParam, success){
    var parseActionName = "doAction" + actionName;
    this[actionName] = this + "&runAction=" + actionName;
    var splits = methodName.split(".");
    if(splits.length == 2){
        var ctrl = splits[0];
        if(!window[ctrl]){//如果未引入ctrl，重新加载JS文件。
            dhx.require("../../../dwr/interface/" + ctrl + ".js");
            dhx.extend(this.dwr, window[ctrl]); //依次继承Dwr中的方法。
        }
    }
    arguments = Array.prototype.slice.call(arguments)
    //参数处理
    if(arguments.length > 2){
        if(arguments[2] && typeof arguments[2] == "object" &&
                arguments[2]._isBiDwrMethodParam){//如果是biDwrMethodParam组件对象
            dwrParam = arguments[2];
        } else{//如果只是普通参数。
            var param = [];
            for(var i = 2; i < arguments.length - 1; i++){
                param[i - 2] = arguments[i]
            }
            dwrParam = param;
        }
        //读取最后一个回调函数,这里将Dwr 简单化，回调只能放在最后一个参数
        if(arguments.length > 2 && arguments[arguments.length - 1]){
            if(typeof arguments[arguments.length - 1] == "function"){
                success = arguments[arguments.length - 1];
                //以元数据形式调用dwr
                success = {callback:success};
            } else if(typeof arguments[arguments.length - 1] == "object"){
                if(arguments[arguments.length - 1].callback){
                    success = arguments[arguments.length - 1];
                } else{
                    success = undefined;
                }
            } else{
                success = undefined;
            }
            //如果没有回调函数，最后一个参数为参数
            if(dhx.isArray(dwrParam) && !success){
                dwrParam.push(arguments[arguments.length - 1]);
            }
        } else{
            success = undefined;
        }
    }
    this._action[actionName] = {methodName:methodName,dwrParam:dwrParam,success:success ? success : {}};
    var callback = this._action[actionName].success.callback;
    /**
     * 定义doActionXXX方法，如果定义了一个anction，调用此方法可以执行定义好的Dwr方法。
     * @param afterCallfun dwr回调函数，如果在添加Action的时候已经定义了回调函数，那么此函数的执行原则为：
     * 1、先执行添加Action时候的回调函数，如果此回调函数返回false，此回调函数不能执行。
     * 2、第一个回调函数返回data，此回调函数得以继续执行，此回调函数也作为第二个参数传递给第一个回调函数
     * 所以第一个回调函数可以显示的调用第二个参数然后执行此回调函数。
     * @param extendParam  执行Action时的扩展参数，可为biDwrMethodParam控件和普通参数，当为biDwrMethodParam控件
     * 时，为避免其复杂性，规定只能有一个biDwrMethodParam控件，即程序如果读取到是此控件。此参数索引位让出，其他普通参数依次提前。
     * 对同一索引的参数的合并规则，取决于biDwrMethodParam控件合并规则的具体定义。最后得出的参数是所有参数定义合并集合。
     * 如果是普通参数，则参数将依次添加到DWR方法中。
     */
    this[parseActionName] = function(afterCallfun, extendParam){
        var action = this._action[actionName];
        var param = action.dwrParam;
        if(param){
            param = dhx.isArray(param) ? param.slice() : param.buildParam();
        }
        var afterCall = null;
        if(typeof afterCallfun == "function"){
            afterCall = afterCallfun;
        }
        action.success.callback = function(data){
            if(callback){//调用dwrCaller中的回调进行数据整理
                var temp = callback.apply(window, [data,afterCall]);
                if(temp != false){
                    data = temp || data;
                    if(afterCall){
                        afterCall(data);
                    }
                }
                return true;
            } else{
                if(afterCall){
                    afterCall(data);
                }
            }
        }
        param = param || [];
        var commonParam = [];
        //扩展参数扫描。
        for(var i = afterCall ? 1 : 0; i < arguments.length; i++){
            if(typeof arguments[i] == "object" && arguments[i]._isBiDwrMethodParam){
                var tempParam = arguments[i].buildParam();
                for(var iii = 0; iii < tempParam.length; iii++){
                    param[iii] = biDwrMethodParam.connectParam(param[iii], tempParam[iii]);
                }
            } else{//普通参数直接加在末尾。
                commonParam.push(arguments[i]);
            }
        }
        //处理普通参数，依次加在末尾处
        for(var k = 0; k < commonParam.length; k++){
            param.push(commonParam[k]);
        }
        var dwrFun = null;
        var obj = window;
        //方法处理
        var split;
        if((split = action.methodName.split(".")).length == 2){ //如果是"XXX.XXX"形式，在window上执行
            dwrFun = window[split[0]][split[1]];
        } else{ //只有方法名，在caller上执行
            dwrFun = this.dwr[action.methodName];
            obj = this;
        }
        var paramCount = 0;//参数个数
        /**
         * 由于Dwr2对参数个数检查严格，而有些情况比如分页查询的时候，第一个load分页参数不一定会传至后台，所以会造成参数不匹配的情况发生
         * 这里扫描Dwr函数的形参个数，个数不足以null补齐，个数多了则弹出error，程序停止执行。JS检查DWR参数个数的原理是利用正则表达式去
         * 匹配函数的toString方法转化后的字符串，匹配规则是否匹配"function (p1,p2。。。){...}"函数形式
         */
        var reg = /function\s*\(((\s*[a-zA-Z_]\w*\s*,?)*)\s*\)[\s\n\r]*\{[\s\S]*\}$/;
        var match = reg.exec(dwrFun.toString());
        if(match && match.length > 0){
            var dwrParamStr = match[1];
            paramCount = dwrParamStr.split(",").length;
        }
        if(param.length + 1 > paramCount){
            alert("所传参数个数超过了DWR方法定义的长度:" + (paramCount - 1));
            return;
        } else if(param.length + 1 < paramCount){
            for(var ii = param.length; ii < paramCount - 1; ii++){
                param[ii] = null;//以null做补充占个位数
            }
        }
        param.push(action.success);
        //调用Dwr方法
        dwrFun.apply(obj, param);
    }
}
/**
 * 添加一个非常灵活的Action，该Action只需提供一个回调函数，该回调函数中可以用Dwr任意访问数据，可以做其他很复杂的逻辑操作，
 * 全由用户自定义
 * @param actionName
 * @param callBack 如果该Action和一些组件联合使用，如grid，tree,treeGrid，callback,load的时候会需要控件做后续操作，比如
 * 数据加载解析动作，这个时候callBack会有afterCall函数，如果访问的过程中控件会传送其他参数，callBack会有第二个参数param,
 * 以对象形式表示，比如分页参数:{posStart:0,count:10}
 */
biDwrCaller.prototype.addAction = function(actionName, callBack){
    var parseActionName = "doAction" + actionName;
    this._action[actionName] = {success:callBack};
    this[actionName] = this + "&runAction=" + actionName;

    /**
     * 定义doActionXXX方法，如果定义了一个anction，调用此方法可以执行定义好的Dwr方法。
     * @param afterCallfun 控件后续操作函数，afterCall
     * @param extendParam  控件访问传的参数。
     */
    this[parseActionName] = function(afterCallfun, extendParam){
        var action = this._action[actionName];
        var success = action.success; //用户自定义访问数据，处理逻辑函数。
        success(afterCallfun, extendParam);//调用用户自定义函数，传参数。
    }
}
/**
 *
 * @param actionName
 */
biDwrCaller.prototype.getActionParam = function(actionName){
    return this._action[actionName].dwrParam;
}
biDwrCaller.prototype.setActionParam = function(actionName, param){
    return this._action[actionName].dwrParam = param;
}
/**
 * 执行指定的actionName
 * @param actionName
 */
biDwrCaller.prototype.executeAction = function(actionName){
    if(this["doAction" + actionName]){
        this["doAction" + actionName].call(this);
    }
}
/**
 * 添加加载数据action
 * @param methodName
 * @param dwrParam
 * @param success
 */
biDwrCaller.prototype.loadAction = function(methodName, dwrParam, success){
    var agrs = Array.prototype.slice.call(arguments);
    agrs.unshift("load");
    this.addAutoAction.apply(this, agrs);
}


/***************************************************************************
 *                  dhx.ajax组件扩展，主要新增dwr访问方式
 ***************************************************************************/
dhx.oldAjax = dhx.ajax;
/**
 * 重写ajax构造函数，调用dwr
 * @param url
 * @param call
 * @param master
 */
dhx.ajax = function(url, call, master){
    //if parameters was provided - made fast call
    if(arguments.length !== 0){
        if(!biDwrCaller.isDwrCallerObject(url)){
            return dhx.oldAjax(url, call.master);
        }
    }
    return new dhx.dwr(url, call, master);//调用dwr

};
/**
 * 扩展dhtmlx ajax函数，新增dwr执行方法。
 * @param bidwrMethod
 * @param action
 */
dhx.dwr = function(bidwrMethod, call, master){
    this._ajax = new dhx.oldAjax(); //原ajax访问方式
    this._async = true;//表示异步访问。
    this._post = true;
    if(arguments.length != 0 && bidwrMethod){
        var http_request = new dhx.dwr();
        if(master){
            http_request.master = master;
        }
        http_request.post(bidwrMethod, null, call);
    }
    if(!this.getXHR){
        return new dhx.dwr();
    }
    return this;
}

dhx.dwr.prototype = {
    //creates xmlHTTP object
    getXHR:function(){
        if(dhx.env.isIE){
            return new ActiveXObject("Microsoft.xmlHTTP");
        } else{
            return new XMLHttpRequest();
        }
    },
    /**
     * 用dwr发送数据，
     * @param dwrcaller biDwrCaller对象
     * @param params 追加参数。
     * @param call  回调函数。
     */
    send:function(url, params, call){
        if(typeof call == "function"){
            call = [call]
        }
        var dwrcaller = biDwrCaller.format(url);
        var action = dwrcaller.runAction;
        var appendParam = dwrcaller.appendParam || [];
        if(params){
            //如果是数组，直接连接
            if(dhx.isArray(params)){
                appendParam = appendParam.concat(params);
            } else{//如果是Key=Value形式,解析进去
                params = biDwrMethodParam.urlParamFormat("&" + params);
                if(params._biDwrMethodParam){
                    appendParam.push(params._biDwrMethodParam);
                    delete params._biDwrMethodParam;
                }
                if(!Tools.isEmptyObject(params)){
                    appendParam.push(params);
                }
            }

        }
        dwrcaller = dwrcaller.dwrCaller;
        if(!dwrcaller["doAction" + action]){
            alert("未传入dwr Caller对象");
            return "";
        }
        //设置post参数
        if(typeof dwrcaller._action[action].success == "object"){ //自动方式
            dwrcaller._action[action].success.httpMethod = this._post ? "POST" : "GET";
            dwrcaller._action[action].success.async = this._async;
        }
        var self = this;
        var caller = dwrcaller["doAction" + action];
        var after = function(data){
            //调用此时回调进行后续操作
            if(call && self){
                for(var i = 0; i < call.length; i++)    //there can be multiple callbacks
                {
                    if(call[i]){
                        call[i].call((self.master || self), data);
                    }
                }
            }
            //            self.master=null;
            //            call=self=null;	//anti-leak
        };
        appendParam.unshift(after);
        caller.apply(dwrcaller, appendParam);
        //还原同步异步初始值
        this._async = true;
        return this; //return XHR, which can be used in case of sync. mode
    },
    //GET request
    get:function(url, param, call){
        if(!biDwrCaller.isDwrCallerObject(url)){
            return this._ajax.get(url, param, call);
        }
        this._post = false;
        return this.send(url, param, call);
    },
    //POST request
    post:function(url, param, call){
        if(!biDwrCaller.isDwrCallerObject(url)){
            return this._ajax.post(url, param, call);
        }
        this._post = true;
        return this.send(url, param, call);
    },
    sync:function(){
        this._async = false;//同步访问。
        return this;
    }
};

/***************************************************************************
 *    dtmlXMLLoaderObject组件扩展，DHTMLX大部分组件都是由此组件访问数据
 *    主要新增dwr访问方式
 ***************************************************************************/
/**    重写组建的Load XMl访问方法，新增dwr访问方式。
 *     @desc: load XML
 *     @type: private
 *     @param: filePath - xml file path
 *     @param: postMode - send POST request
 *     @param: postVars - list of vars for post request
 *     @topic: 0
 */
dtmlXMLLoaderObject.prototype.oldloadXML = dtmlXMLLoaderObject.prototype.loadXML;//缓存一个函数。
dtmlXMLLoaderObject.prototype.loadXML = function(filePath, postMode, postVars, rpc){
    if(!biDwrCaller.isDwrCallerObject(filePath)){
        return this.oldloadXML(filePath, postMode, postVars, rpc);
    }
    var dhtmlObject = this;
    return new dhx.dwr().send(filePath, postVars, function(data){
        this._data = data;
        this.oldxmlDoc = this.xmlDoc;
        this.xmlDoc = {};
        //写一个函数，便于执行
        //eval("data="+loader.xmlDoc.responseText);语句。
        this.xmlDoc.responseText = "(function(obj){return obj._data})(this)";
        this.xmlDoc.responseXML = "(function(obj){return obj._data})(this)";
        if(typeof dhtmlObject.onloadAction == "function"){
            try{
                dhtmlObject.onloadAction(dhtmlObject.mainObject, null, null, null, data);
            } catch(e){ //如果解析失败，可能是由于格式问题，因为DHTMX默认是XMl解析。
                if(typeof data == "object"){
                    if(dhtmlObject.mainObject.parse){ //grid/gridtree/view/chat
                        dhtmlObject.mainObject.parse(data, "json");
                    } else if(dhtmlObject.mainObject.loadJSONObject){ //tree
                        dhtmlObject.mainObject.loadJSONObject(data);
                    }
                }
            }
        }
        if(dhtmlObject.waitCall){
            dhtmlObject.waitCall.call(this, this);
            dhtmlObject.waitCall = null;
            this.xmlDoc = this.oldxmlDoc;
            this.oldxmlDoc = null;
            this._data = null;
        }
    });
}

/***************************************************************************
 *                   dataProcessor组件扩展，
 ***************************************************************************/
/**
 * dataProcessor发送数据核心，扩展参数统一以List<Map> 形式发送。
 * @param a1 所有数据
 * @param rowId  行ID
 */
dataProcessor.prototype._oldSendData = dataProcessor.prototype._sendData;
dataProcessor.prototype._sendData = function(a1, rowId){
    if(!biDwrCaller.isDwrCallerObject(this.serverProcessor)){
        return this._oldSendData(a1, rowId);
    }
    if(!a1){
        return;
    }
    if(!this.callEvent("onBeforeDataSending", rowId ? [rowId,this.getState(rowId),a1] : [null, null, a1])){
        return false;
    }
    if(rowId){
        this._in_progress[rowId] = (new Date()).valueOf();
    }
    var a2 = new dtmlXMLLoaderObject(function(that, b, c, d, xml){
        var atag = xml.data;
        for(var i = 0; i < atag.length; i++){
            var btag = atag[i];
            var action = btag["type"];
            var sid = btag["sid"];
            var tid = btag["tid"];
            that.afterUpdateCallback(sid, tid, action, btag);
        }
        that.finalizeUpdate();
    }, this, true);
    var a3 = this.serverProcessor + (this._user ? (getUrlSymbol(this.serverProcessor) +
            ["dhx_user=" + this._user,"dhx_version=" + this.obj.getUserData(0, "version")].join("&")) : "");
    var data = [];
    this._waitMode++;
    if(typeof a1 == "string"){
        data.push(a1);
    } else{
        if(typeof rowId != "undefined"){
            a1[rowId] = a1;
        }
        //update，delete，insert参数各自声明。
        var updated = [];
        var deleted = [];
        var inserted = [];
        for(var key in a1){
            var temp = {};
            for(var subKey in a1[key]){
                //判断此数据的操作状态。
                if(subKey.indexOf("!nativeeditor_status") != -1){
                    temp["!nativeeditor_status"] = a1[key][subKey];
                    eval(a1[key][subKey]).push(temp);
                }
                temp[subKey] = a1[key][subKey];
            }
            temp.rowId = key;
            data.push(temp);
        }
        if(a3.indexOf("runAction") == "-1"){//未定义提交Action，则安装模式依次提交。
            if(updated.length > 0){//提交更改数据
                a2.loadXML(a3 + ((a3.indexOf("?") != -1) ? "&" : "?") + "runAction=update", true, updated);
            }
            if(deleted.length > 0){
                a2.loadXML(a3 + ((a3.indexOf("?") != -1) ? "&" : "?") + "runAction=deleted", true, deleted);
            }
            if(inserted.length > 0){
                a2.loadXML(a3 + ((a3.indexOf("?") != -1) ? "&" : "?") + "runAction=insert", true, inserted);
            }
        } else{
            a2.loadXML(a3, true, data);
        }
        return;
    }
    a2.loadXML(a3, true, data);
}

/***************************************************************************
 *                   dhtmlXGridObject组件扩展，
 ***************************************************************************/
/**
 * DHTMX组件扩展，新增用户自定义格式化函数。
 * @param columnIndex  行号
 * @param formater formater函数，有三个参数，value：原value，rowData：行数据，columnIndex：行索引。
 */
dhtmlXGridObject.prototype.setColumnCustFormat = function(columnIndex, formater){
    this._customerFormater = this._customerFormater || [];
    this._customerFormater[columnIndex] = formater;

}

/**
 * DHTMX组件扩展,实现系统默认分页，并采用DWR访问方法，采用工具条形式在表尾添加分页
 * @param pageSize 每页显示行数
 */
dhtmlXGridObject.prototype.defualtPaging = function(pageSize){
    pageSize = pageSize || this.rowsBufferOutSize || 10;
    //添加分页工具条节点。将dhtmlXGridObject table再次包装。
    var newTable = document.createElement("table");
    var genstr = Tools.genStr(8);
    //    alert('<tr><td id="_inner_context_'+genstr+'"></td></tr><tr> <td><div id="recinfoArea_'+genstr+'"></div></td></tr>')
    newTable.cellpadding = "0";
    newTable.style.width = "100%";
    var tbody = document.createElement("TBODY");
    var tr = document.createElement("tr");
    var td = document.createElement("td");
    td.id = "_inner_context_" + genstr;
    tr.appendChild(td);
    tbody.appendChild(tr);

    tr = document.createElement("tr");
    td = document.createElement("td");
    td.innerHTML = '<div id="recinfoArea_' + genstr + '"></div>';
    tr.appendChild(td);
    tbody.appendChild(tr);

    newTable.appendChild(tbody);
    this.entBox.parentNode.insertBefore(newTable, this.entBox);
    this.attachToObject(dwr.util.byId("_inner_context_" + genstr));
    //设置分页。
    this.enablePaging(true, pageSize, 3, 'recinfoArea_' + genstr);
    this.setPagingSkin("toolbar", getSkin());
    /*this.attachEvent("onBeforePageChanged", function(currentPage, nextPage) {
     var page = {posStart:(nextPage - 1) * this.rowsBufferOutSize + 1,count:this.rowsBufferOutSize};
     this.loadData(page);
     return true;
     });*/
};

/**
 * 重写cell4方法，用于生产cell对象，用AOP方式加入用户的格式化函数显示。
 * @param cell
 */
dhtmlXGridObject.prototype.cells4 = function(cell){
    var type = window["eXcell_" + (cell._cellType || this.cellType[cell._cellIndex])];
    //        alert("eXcell_"+(cell._cellType||this.cellType[cell._cellIndex]))
    var customerFormater = this._customerFormater ? this._customerFormater[cell._cellIndex] : null;
    if(type){
        var typeObject = new type(cell);
        if(customerFormater){
            //重写其setValue方法
            var orgMethod = typeObject['setValue'];
            typeObject['setValue'] = function(value){
                typeObject._cellOrgValue = value;
                //之前调用用户的formater
                if(typeof customerFormater == "string"){
                    customerFormater = window[customerFormater];
                }
                value = customerFormater.call(window, value, cell.parentNode.getAttribute("_attrs"), cell._cellIndex);
                typeObject._cellFormatValue = value;
                orgMethod.call(typeObject, value);
            }
        }
        return typeObject;
    }
}

/**
 * 用户自定义表头关键字函数扩展，使用关键字"#checkBox"即可在表头生成一个选择框。
 * 当点击选择框选中，则对应列所有都选中，当点击该选择框不选，则改列所有选择框
 * 取消选择。注：只对当前页进行操作。
 * @param tag
 * @param index
 * @param data
 */
dhtmlXGridObject.prototype._in_header_checkBox = function(tag, index, data){
    var html = "";
    for(var i = 0; data && i < data.length; i++){
        html += data[i];
    }
    var grid = this;
    html = "<img src='" + grid.imgURL + "item_chk0.gif'>";
    tag.innerHTML = html;
    var image = tag.lastChild;
    image.setAttribute("_checked", false);
    Tools.addEvent(image, "click", function(){
        var setCheckedRows = function(cInd, v){
            grid.forEachRowA(function(id){
                var state = grid.getStateOfView();
                var rowIndex = grid.getRowIndex(id);
                if(rowIndex >= state[1] && rowIndex <= state[2]){ //当前页全选或者当前页全不选
                    if(grid.cells(id, cInd).isCheckbox()){
                        grid.cells(id, cInd).setValue(v);
                    }
                }
            })
        }
        var checked = image.getAttribute("_checked");
        if(checked != undefined && checked != null && (checked == "true" || checked == true)){
            setCheckedRows(tag.parentNode._cellIndex, 0);
            image.setAttribute("_checked", false);
            image.setAttribute("src", image.getAttribute("src").replace("item_chk1", "item_chk0"));
        } else{
            setCheckedRows(tag.parentNode._cellIndex, 1);
            image.setAttribute("_checked", true);
            image.setAttribute("src", image.getAttribute("src").replace("item_chk0", "item_chk1"));
        }
    });
    tag.style.verticalAlign = "middle";
    if(index == 0){
        this.setColumnColor("#CCE2FE");
    }
}
/**
 * 显示行号，默认显示在第一列
 */
dhtmlXGridObject.prototype.showRowNumber = function(ind){
    var index = ind || 0;
    this.insertColumn(index, "&nbsp;", "cntr", 30, "na", "center", "middle", null, "#CCE2FE");
}

/**
 * 重写排序方法，用于分页情况下动态数据未加载完成时的排序，原DHTMLX存在缺陷。
 * @param col
 * @param type
 * @param order
 */
dhtmlXGridObject.prototype.sortRows = function(col, type, order){
    //default values
    order = (order || "asc").toLowerCase();
    type = (type || this.fldSort[col]);
    col = col || 0;

    if(this.isTreeGrid()){
        this.sortTreeRows(col, type, order);
    } else{

        var arrTS = {};

        var atype = this.cellType[col];
        var amet = "getValue";

        if(atype == "link"){
            amet = "getContent";
        }
        if(atype == "dhxCalendar" || atype == "dhxCalendarA"){
            amet = "getDate";
        }
        try{
            for(var i = 0; i < this.rowsBuffer.length; i++){
                if(this.rowsBuffer[i])//如果数据已经加载，modify by 张伟
                {
                    arrTS[this.rowsBuffer[i].idd] = this._get_cell_value(this.rowsBuffer[i], col, amet);
                }
            }
            this._sortRows(col, type, order, arrTS);
        } catch(e){
        }
    }
    this.callEvent("onAfterSorting", [col,type,order]);
};
/**
 * 添加默认验证失败，验证成功效果
 */
dhtmlXGridObject.prototype.defaultValidateEvent = function(){
    var that = this;
    this.attachEvent("onLiveValidationError", function(id, ind, value, element, rule){
        var msg = element._errorMsg;
        dhtmlxValidation.tipEvent(element, false, msg);
        return false;
    });
    this.attachEvent("onLiveValidationCorrect", function(id, ind, value, element, rule){
        var msg = element._errorMsg;
        dhtmlxValidation.tipEvent(element, true, msg);
        return false;
    });
    this.attachEvent("onValidationError", function(id, ind, value, rule){
        var cell = that.cells(id, ind).cell;
        if(dhx.isArray(rule)){
            rule = rule[0];
        }
        var msg = dhtmlxValidation["is" + rule.split('[')[0]]._errorMsg;
        dhtmlxValidation.tipEvent(cell, false, msg);
        return false;
    });
    this.attachEvent("onValidationCorrect", function(id, ind, value, rule){
        var cell = that.cells(id, ind).cell;
        if(dhx.isArray(rule)){
            rule = rule[0];
        }
        var msg = dhtmlxValidation["is" + rule.split('[')[0]]._errorMsg;
        dhtmlxValidation.tipEvent(cell, true, msg);
        return false;
    });
};

/**
 * 对DHTMLX GRID控件进行扩展，提供一个静态方法用于产生dhtmlXGridObject实例。
 * 产生实例的逻辑如下：1、如果对应的id是div节点。则直接调用构造方法dhtmlXGridObject产生一个普通实例。
 *                     2：如果对应的id是table节点，则调用dhtmlXGridFromTable方法解析HTML产生一个实例
 * 具体文档参考DHTMX文档。除此之外，添加了一些自定义属性，如下所示：
 *  <table id="user"  style="width:100%;"  imgpath="${pageContext.request.contextPath}/resource/dhtmlx/imgs/"
 border="1" lightnavigation="true" paging="true" pageSize="15" autoHeight="true">
 <tr heightStyle="height:30px">
 <%--<td width="12"></td>--%>
 <td  width="20" align="center" type="cntr"></td>
 <td  width="30" align="center" type="ch">{#checkBox}</td>
 <td  width="*" align="center">邮件</td>
 <td  width="*" align="center">姓名</td>
 <td  width="*" align="center" >手机号码</td>
 <%--<th field="deptid" width="97" editor="text" align="center">部门</th>--%>
 <td  width="0" align="center">岗位</td>
 <td  width="*" align="center" >岗位</td>
 <td  width="*"  align="center" customerFormater="stateFormatter">状态</td>
 <td  width="*"  align="center" customerFormater="magFlagFormatter">管理权限</td>
 </tr>
 </table>
 新增属性：新增分页属性， 在table节点中定义 paging="true" pageSize="15"，则可实现默认分页。
 autoHeight：自动高度，
 tr节点中设置 heightStyle 可以设置高度等样式
 column 如果没有设置 type属性，默认为不可编辑，如果没有设置sort属性，默认不可排序
 调整表头与表数据相同的对齐方式。
 customerFormater:自定义格式化函数。
 * @param id  节点ID 或者DOM节点
 */
dhtmlXGridObject.instance = function(id){
    var gridClass = null;
    var node = null;
    if(dwr.util._isHTMLElement(id)){
        node = id;
    } else{
        node = dwr.util.byId(id);
    }
    if(!node){
        return null;
    } else{
        if(node.nodeName.toLowerCase() == "table"){
            var _customerFormater = [];
            var tr = node.rows[0];//取得第一个tr节点
            for(var i = 0; i < tr.cells.length; i++){
                var td = tr.cells[i];
                var type = td.getAttribute("type");//找到type属性
                if(!type){ //如果没有type属性，设置默认的“ron”
                    td.setAttribute("type", "ron");
                }
                var sort = td.getAttribute("sort");
                if(!sort){
                    td.setAttribute("sort", "na");//默认不排序
                }
                var algin = td.getAttribute("align") || "left";
                //设置表头与列一样的对齐方式
                var text = td.innerHTML;
                var newHtml = '<div style="text-align:' + algin + ';">' + text + "</div>";
                td.innerHTML = newHtml;
                //如果有用户自定义formater
                var customerFormater = td.getAttribute("customerFormater");
                if(customerFormater){
                    _customerFormater[i] = customerFormater;
                }
            }
            gridClass = new dhtmlXGridFromTable(node);
            //如果设置了customer，加载用户自定义格式化函数
            for(var i = 0; i < _customerFormater.length; i++){
                if(_customerFormater[i]){
                    gridClass.setColumnCustFormat(i, _customerFormater[i]);
                }
            }
            if(tr.getAttribute("heightStyle")){
                gridClass.setStyle(tr.getAttribute("heightStyle"));
            }
            //分页设置。
            var paging = node.getAttribute("paging");
            paging = (paging != undefined && paging != null && paging == "true");
            if(paging){
                gridClass.defualtPaging(node.getAttribute("pageSize"));
            }
            //高度，宽度样式设置
            var autoHeight = node.getAttribute("autoHeight");
            if(!node.style.width){
                gridClass.enableAutoWidth(true);
            } else if(autoHeight != undefined && autoHeight != null && autoHeight == "true"){
                gridClass.enableAutoHeight(true);
            }
            gridClass.setSizes();
        } else{
            gridClass = new dhtmlXGridObject(node);
        }
        if(gridClass != null){
            //设置表皮肤设置。
            gridClass.setImagesPath(getDefaultImagePath());
            gridClass.setSkin(getSkin());
            return gridClass;
        }
    }
};

/***************************************************************************
 *          dhtmlxValidation组件扩展，主要新增验证方式，远程验证等。
 ***************************************************************************/

/**
 * 以下是新增的验证规则
 */
/**
 * 最小值验证，用法，设置validate="Min[X]" 即可
 * @param value
 * @param min
 */
dhtmlxValidation.isMin = function(value, min){
    return  value >= min;
};
/**
 * 最大值验证,用法，设置validate="Max[X]" 即可
 * @param value
 * @param max
 */
dhtmlxValidation.isMax = function(value, max){
    return  value <= max;
};
/**
 * 范围区间验证，用法，设置validate="Range[X Y]" 即可
 * @param value
 * @param min
 * @param max
 */
dhtmlxValidation.isRange = function(value, min, max){
    return  value >= min && value <= max;
};

/**
 * 输入最小长度验证,中文算两个字符，用法，设置validate="MinLength[X]" 即可
 * @param value
 * @param minLength
 */
dhtmlxValidation.isMinLength = function(value, minLength){
    var cArr = Tools.trim(value).match(/[^\x00-\xff]/ig);
    var len = Tools.trim(value).length + (cArr == null ? 0 : cArr.length);
    return  len >= minLength;
};
/**
 * 输入最大长度值验证，中文算两个字符，用法：设置validate="MaxLength[X]" 即可
 * @param value
 * @param maxLength
 */
dhtmlxValidation.isMaxLength = function(value, maxLength){
    var cArr = Tools.trim(value).match(/[^\x00-\xff]/ig);
    var len = Tools.trim(value).length + (cArr == null ? 0 : cArr.length);
    return  len <= maxLength;
};
/**
 * 与另一个HTML元素输入相同的值判断,用法：设置validate="EqualTo[元素ID]" 即可
 * @param value
 * @param org
 */
dhtmlxValidation.isEqualTo = function(value, org){
    var orgValue = dwr.util.getValue(org);
    return   value == orgValue;

};
/**
 * 输入值是否是中文判断
 * @param value
 */
dhtmlxValidation.isChinese = function(value){
    return /[^\u4E00\-\u9FA5]/g.test(value);
};
/**
 * 输入值是否是正整数判断
 * @param value
 */
dhtmlxValidation.isPositiveInt = function(value){
    return /^[0-9]*[1-9][0-9]*$/.test(value);
};
/**
 * 判断是否是输入字母
 * @param value
 */
dhtmlxValidation.isAlpha = function(value){
    return /^[a-zA-Z]*$/.test(value);
};
/**
 * 判断是否是邮政编码
 * @param value
 */
dhtmlxValidation.isZip = function(value){
    return /^[1-9][0-9]{5}$/.test(value);
};
/**
 * 是否是电话号码
 * @param value
 */
dhtmlxValidation.isMobile = function(value){
    return /^1[3-9]\d{9}$/.test(value);
}
/**
 * 远程判断,远程返回true即验证成功，返回其他值验证失败，并作为验证提示，此用dwr的同步访问方法。
 * 用法：设置validate="Remote[dwrCaller组件]" 即可
 * @param value
 * @param dwr
 */
dhtmlxValidation.isRemote = function(value, dwr){
    var result = null;
    if(arguments.length > 2){ //因为dwr参数是以“&”形式区分，所以在checkValue时，可能会切断，这里重新组合
        for(var i = 2; i < arguments.length; i++){
            dwr += (dwr.indexOf("?") != -1 ? "&" : "?") + arguments[i];
        }
    }
    dhx.ajax().sync().post(dwr, value, function(data){
        result = data;
    });
    //数据的返回应为一个object,格式应为{result:true/false,message:XXXX};
    //亦支持验证成功即返回true，失败返回提示消息。
    if(typeof result == "object"){
        return (result.result == "true" || result.result == true) ? true : result.message;
    } else{
        return result;
    }
}
/**
 * 用回调函数实现验证逻辑，用法：设置validate="ValidByCallBack[函数名&参数1&参数2...]" 即可
 * @param value
 * @param callBack  回调函数名，该函数必须在window中已定义。不支持一个对象中的函数。
 * 注意：该回调函数的返回结果可以有以下两种方式：
 * 1、对象式:例，验证成功返回:{result:true,message:""},验证失败返回:{result:false,message:"XXX"};
 * 2、验证成功可以直接返回true，验证失败直接返回一个字符串表示提示信息。
 * @param params  参数集。
 */
dhtmlxValidation.isValidByCallBack = function(value, callBack, params){
    var fun = window[callBack];
    if(!fun || typeof fun != "function"){
        alert("回调验证函数未定义,默认验证成功！");
        return true;
    }
    var funParam = [value];
    for(var i = 2; i < arguments.length; i++){
        funParam.push(arguments[i]);
    }
    var result = fun.apply(window, funParam);
    if(typeof result == "object"){
        return (result.result == "true" || result.result == true) ? true : result.message;
    } else{
        return result;
    }
}
/**
 * 重写checkInput方法
 * @param input
 * @param rule
 */
dhtmlxValidation.checkInput = function(input, rule){
    return this.checkValue(input, rule);
};
/**
 * 重写实际验证方法，加入本系统逻辑
 * @param value
 * @param rule
 */
dhtmlxValidation.checkValue = function(value, rule){
    var input = dwr.util._isHTMLElement(value) ? value : null;
    value = input ? dwr.util.getValue(value) : value;
    var param = [];
    var required = false;//是否为必须。
    var remoteIndex = -1;//远程验证索引位。
    if(typeof rule == "string"){
        //匹配rule1[param1,param2],rule2形式。
        var reg = /(\w+)(\[([^\,\s&\[\]]*[\,|\s|&]?)*\])?[\,|&]?/g;
        var match = null;
        var temp = rule;
        rule = [];
        while(match = (reg.exec(temp))){
            var tempRule = match[1];
            if(match[2]){
                var paramTemp = [];
                var regParam = /([^\,\s&\[\]]+)[\,|\s|&]?/g;
                regParam.lastIndex = 1;
                var paramMatch = null;
                while(paramMatch = (regParam.exec(match[2]))){
                    paramTemp.push(paramMatch[1]);
                }
                param[rule.length] = paramTemp;
            }
            if(!this["is" + tempRule]){
                alert("Incorrect validation rule: " + tempRule);
            }
            this["is" + tempRule]._errorMsg = null;
            if(tempRule == "NotEmpty"){//如果有非空选项，特殊处理
                required = true;
                continue;
            } else if(tempRule == "Remote"){
                remoteIndex = rule.length;
            }
            rule[rule.length] = tempRule;
        }
    }
    //验证顺序处理，远程验证需在最后，为空判断需要在最前。
    if(required){//如果必填，需先验证。
        rule.unshift("NotEmpty");
        param.unshift([]);
    } else if(remoteIndex >= 0 && remoteIndex < rule.length - 1){
        //移动元素，将远程验证放在最后。这样可以减少服务器交互次数。
        var ruleRemote = rule[remoteIndex];
        var paramRemote = param[remoteIndex];
        rule[remoteIndex] = rule[rule.length - 1];
        param[remoteIndex] = param[rule.length - 1];
        rule[rule.length - 1] = ruleRemote;
        param[rule.length - 1] = paramRemote;
    }
    for(var i = 0; i < rule.length; i++){
        if(!required && !value){ //如果不是必填，且value为空。不做验证
            return true;
        }
        var checkParam = (param[i] ? (dhx.isArray(param[i]) ? param[i] : [param[i]]) : []).slice();
        checkParam.unshift(value);
        var checkResult = this["is" + rule[i]].apply(this, checkParam);
        if(checkResult == true || checkResult == "true"){ //返回true才能算作校验成功。
        } else{
            var errorMsg = null;
            if(typeof checkResult == "string"){ //如果返回是字符串。则默认是错误信息
                errorMsg = checkResult;
            } else{ //返回false，从模板中取
                errorMsg = this.validateErrorMag["is" + rule[i]];
                if(errorMsg && param[i]){
                    errorMsg = errorMsg.replace(/\{(\d)\}/g, function($1){
                        return param[i][arguments[1]]
                    });
                }
            }
            //记录errorMsg
            if(input){
                input._errorMsg = errorMsg;
            }
            this["is" + rule[i]]._errorMsg = errorMsg;
            return false;//只要一个未验证通过即返回false
        }
    }
    if(rule.length == 0){
        throw new Error("rule has probleam");
    }
    return true;
};
/**
 *  根据一个rule字符串返回一个rule函数。未找到返回null。
 * @param ruleStr
 */
dhtmlxValidation.getRule = function(ruleStr){
    var reg = /(\w+)(\[([^\,\s&\[\]]*[\,|\s|&]?)*\])?/g;
    var match = null;
    match = reg.exec(ruleStr);
    if(match){
        var tempRule = match[1];
        return this["is" + tempRule];
    }
    return null;
}
/**
 * 显示验证提示信息
 * @param target
 * @param message
 */
dhtmlxValidation.showTip = function(target, msg){
    var box = $(target);
    if(!box._tipUid){
        box._tipUid = dhx.uid();
    }
    var owner = box._tipUid + "_validate_tip_";
    var tip = $(owner); //tipId
    if(!tip){
        tip = dhx.html.create("div", {id:owner,"class":"validatebox-tip"},
                              '<span class=validatebox-tip-content id=' + owner +
                                      '_span_validatebox-tip-content></span>' +
                                      '<span class=validatebox-tip-pointer id=' + owner +
                                      '_span_validatebox-tip-pointer></span>');
        document.body.appendChild(tip);
    }
    $(owner + "_span_validatebox-tip-content").innerHTML = msg;
    var offset = dhx.html.offset(box);
    tip.style.display = "block";
    tip.style.left = offset.x + box.offsetWidth;
    tip.style.top = offset.y;
};
/**
 * 隐藏提示信息
 * @param target
 */
dhtmlxValidation.hideTip = function(target){
    var box = $(target);
    var owner = box._tipUid + "_validate_tip_";
    var tip = $(owner);
    if(tip){
        tip.parentNode.removeChild(tip);
        box._tipUid = null;
    }
};
/**
 * 验证后提示信息的事件处理
 * @param target
 * @param result 验证结果。
 */
dhtmlxValidation.tipEvent = function(target, result, msg){
    var box = $(target);
    if(result){//验证成功，去除所有的验证失败默认效果，移除所有验证失败的效果。
        if(box._events){
            for(var i = 0; i < box._events.length; i++){
                dhx.eventRemove(box._events[i]);
            }
            box._events = null;
            dhx.html.removeCss(target, "validatebox-invalid");
            this.hideTip(target);
        }
    } else{//验证失败，添加提示信息
        this.showTip(target, msg);
        dhx.html.addCss(target, "validatebox-invalid");
        if(!box._events){
            var events = [];
            if(dhx.env.isIE){//ie
                events.push(dhx.event(target, "mouseenter", function(){
                    dhtmlxValidation.showTip(target, msg);
                }));//获得焦点显示
                events.push(dhx.event(target, "mouseleave", function(){
                    dhtmlxValidation.hideTip(target, msg);
                }));//失去焦点显示
            } else{ //非浏览器不支持 mouseenter，mouseleave时间，用其他方式模拟。
                events.push(dhx.event(target, "mouseover", function(e){
                    //判断事件触发的源是否是自身。阻止事件传播
                    var t = e.relatedTarget;
                    var t2 = e.target;
                    if(t2 && t && !(t.compareDocumentPosition(this) & 8)){
                        dhtmlxValidation.showTip(target, msg);
                    }
                }));
                events.push(dhx.event(target, "mouseout", function(e){
                    //判断事件触发的源是否是自身。阻止事件传播
                    var t = e.relatedTarget;
                    var t2 = e.target;
                    if(t2 && t && !(t.compareDocumentPosition(this) & 8)){
                        dhtmlxValidation.hideTip(target, msg);
                    }
                }));
            }
            box._events = events;
        }
    }
};


/***************************************************************************
 *          dhtmlxform组件扩展.
 ***************************************************************************/
/**
 * 重写doAddInput 方法，加入验证机制。
 * @param item
 * @param data
 * @param el
 * @param type
 * @param pos
 * @param dim
 * @param css
 */
(function(){
    for(var key in dhtmlXForm.prototype.items){
        var item = dhtmlXForm.prototype.items[key];
        if(item.doAddInput){ //如果有doAddInput方法
            var old = item.doAddInput;
            item.doAddInput = function(item, data, el, type, pos, dim, css){
                //先调用原方法；
                old.call(this, item, data, el, type, pos, dim, css);
                var that = this;
                if(data.validate){
                    var target = item.lastChild.lastChild;
                    //如果设置了验证,新增validate方法。
                    this["validate"] = function(){
                        var r = dhtmlxValidation.checkInput(target, data.validate);
                        var formObj = this._formClass;
                        if(formObj){
                            if(!(formObj.callEvent("onValidate" + (r ? "Success" : "Error"),
                                                   [target,formObj.getItemValue(data.name) || "",r]) === false)){
                                this.setValidateCss(name, r);
                            }
                        }
                        return r;
                    };
                    //添加事件
                    dhx.event(target, "focus", function(){
                        target._validating = true;
                        target._oldValue = undefined;
                        (function(){
                            if(target._validating){
                                if(dwr.util.getValue(target) != target._oldValue){  //值改变才做验证。
                                    target._oldValue = dwr.util.getValue(target);
                                    that.validate();
                                }
                                setTimeout(arguments.callee, 200);
                            }
                        })();
                    });
                    dhx.event(target, "blur", function(){
                        target._validating = false;
                        dhtmlxValidation.hideTip(target);
                    });
                }
            };
        }
    }
})();

/**
 * 默认验证事件处理，格式处理
 */
dhtmlXForm.prototype.defaultValidateEvent = function(){
    for(var key in this.objPull){
        this.objPull[key]._formClass = this;
    }
    //覆盖其验证方法
    this.validate = function(type){
        if(type != "nestedFormCall"){
            if(this.callEvent("onBeforeValidate", []) == false){
                return;
            }
        }
        var completed = true;
        // validation
        for(var a in this.itemPull){
            if(this.itemPull[a]._validate){
                var name = this.itemPull[a]._idd;
                var val = (this.getItemValue(name) || "");
                var item = this.objPull[a];
                var r = true;
                try{
                    r = item.validate(this);
                } catch(e){//用正则表达式匹配
                    for(var q = 0; q < this.itemPull[a]._validate.length; q++){
                        r = r && new RegExp(this.itemPull[a]._validate[q]).test(val);
                        if(!(this.callEvent("onValidate" + (r ? "Success" : "Error"), [name,val,r]) === false)){
                            this.setValidateCss(name, r);
                        }

                    }
                }
            }
            completed = (completed && r);
            if(this.itemPull[a]._list){
                for(var q = 0; q < this.itemPull[a]._list.length; q++){
                    completed = (this.itemPull[a]._list[q].validate("nestedFormCall") && completed);
                }
            }
        }
// after validate
        if(type != "nestedFormCall"){
            this.callEvent("onAfterValidate", [completed]);
        }
        return completed;
    };
    this.attachEvent("onValidateSuccess", function(input, value, result){
        var msg = input._errorMsg;
        dhtmlxValidation.tipEvent(input, true, msg);
        return false;
    });
    this.attachEvent("onValidateError", function(input, value, result){
        var msg = input._errorMsg;
        dhtmlxValidation.tipEvent(input, false, msg);
        return false;
    });
}
/**
 * 覆盖dhtmlx。js中的BUG。
 * @param a
 * @param b
 * @param c
 */
dhtmlXForm.prototype.load = function(a, b, c){
    var d = this;
    d.callEvent("onXLS", []);
    typeof b == "function" && (c = b,b = "xml");
    dhtmlxAjax.get(a, function(e){
        var f = {};
        if(b == "json"){
//            eval("data=" + e.xmlDoc.responseText);//源代码
            eval("f=" + e.xmlDoc.responseText); //修改处
        } else{
            for(var g = e.doXPath("//data/*"),
                        h = 0; h < g.length; h++){
                f[g[h].tagName] = g[h].firstChild ? g[h].firstChild.nodeValue : "";
            }
        }
        var i = a.match(/(\?|\&)id\=([a-z0-9_]*)/i);
        i && i[0] && (i = i[0].split("=")[1]);
        if(d.callEvent("onBeforeDataLoad", [i,f])){
            d.formId = i,d._last_load_data = f,d.setFormData(f),d.resetDataProcessor("updated");
        }
        d.callEvent("onXLE", []);
        c && c.call(this)
    })
};





