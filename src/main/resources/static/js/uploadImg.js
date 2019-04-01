let imgSrc = []; //图片路径
let imgFile = []; //文件流
let imgName = []; //图片名字
//选择图片
function imgUpload(obj) {
    let oInput = '#' + obj.inputId;
    let imgBox = '#' + obj.imgBox;
    let btn = '#' + obj.buttonId;
    let profiles = '#' + obj.profiles;
    let process = '#' + obj.process;
    $(oInput).on("change", function () {
        let fileImg = $(oInput)[0];
        let fileList = fileImg.files;
        for (let i = 0; i < fileList.length; i++) {
            let imgSrcI = getObjectURL(fileList[i]);
            imgName.push(fileList[i].name);
            imgSrc.push(imgSrcI);
            imgFile.push(fileList[i]);
        }
        addNewContent(imgBox);
    });
    $(btn).on('click', function () {
        let data = new Object;
        data[obj.data] = imgFile;
        submitPicture(obj.upUrl, data);
    });
    $(profiles).on("click", function () {
        $.MsgBox.Alert("使用须知", "使用须知：<br/>" +
            "1. 文件名要以姓名作为文件名, 报单人要放在最前面,多个用餐人用\"、\"分割<br/>" +
            "例如: 张三、李四、王五,表示三人用餐, 报单人是张三。<br/>" +
            "<br/>" +
            "2. 一人或多人有多张外卖单要加后缀<br/>" +
            "例如: 张三-1, 张三-2; <br/>" +
            "\t  张三、李四-1, 张三、李四-2<br/>" +
            "\t  <br/>" +
            "3. 一次用餐,多人报销, 请指定一位报销人<br/>" +
            "例如: 张三、李四、王五、小明、小华、小强, 六人在一家店点晚餐外卖,<br/>" +
            "商家满减规则是满90减30, 满100减35,这样两个人定可以减更多,<br/>" +
            "会产生一次订餐多人报单的情况,请指定一位报销人,然后个人私下转账。<br/>" +
            "<br/>" +
            "4. 目前支持美团和饿了么的电子单,纸质单需手动处理。<br/>" +
            "<br/>" +
            "5. 图片格式支持jpg和png。<br/>" +
            "<br/>" +
            "6. 所有电子单要存到同一个文件夹下,按名称递增排列");
    });
    $(process).on('click', function () {
        $.MsgBox.Alert("报销流程", '<img src="images/expenseProcess.png" width="360px" height="800px">');
    });
}

//图片展示
function addNewContent(obj) {
    $(imgBox).html("");
    for (let a = 0; a < imgSrc.length; a++) {
        let oldBox = $(obj).html();
        $(obj).html(oldBox + '<div class="imgContainer">' +
            '<img title=' + imgName[a] + ' alt=' + imgName[a] + ' src=' + imgSrc[a] + ' onclick="imgDisplay(this)">' +
            '<p onclick="removeImg(this,' + a + ')" class="imgDelete">删除</p></div>');
    }
}

//删除
function removeImg(obj, index) {
    imgSrc.splice(index, 1);
    imgFile.splice(index, 1);
    imgName.splice(index, 1);
    let boxId = "#" + $(obj).parent('.imgContainer').parent().attr("id");
    addNewContent(boxId);
}

//触发遮罩层
function LayerShow() {
    $("#loading").css("display", "block");
}

//隐藏遮罩层
function LayerHide() {
    $("#loading").css("display", "none");
}

//上传(将文件流数组传到后台)
function submitPicture(url, data) {
    let formData = new FormData();
    for (let i = 0; i < data.file.length; i++) {
        formData.append("file", data.file[i]);
    }
    $.ajax({
        type: "POST",
        url: url,
        async: true,
        data: formData,
        traditional: true,
        cache: false,
        processData: false,
        contentType: false,
        beforeSend: function () {
            $("#btn").parent().parent().append('<div id="loading"><div id="loading-center">' +
                '<div id="loading-center-absolute"><div class="object" id="object_four"></div>' +
                '<div class="object" id="object_three"></div><div class="object" id="object_two"></div>' +
                '<div class="object" id="object_one"></div></div></div></div>');
            LayerShow();
        },
        complete: function () {
            LayerHide();
        },
        success: function (dat) {
            $.MsgBox.Alert("成功", JSON.stringify(dat));
        },
        error: function (dat) {
            $.MsgBox.Alert("失败", JSON.stringify(dat));
        }
    });
}

//图片灯箱
function imgDisplay(obj) {
    let src = $(obj).attr("src");
    let imgHtml = '<div style="width: 100%;height: 100vh;overflow: auto;background: rgba(0,0,0,0.5);text-align: center;position: fixed;top: 0;left: 0;z-index: 1000;">' +
        '<img src=' + src + ' style="margin-top:100px;width:70%;margin-bottom:100px;"/>' +
        '<p style="font-size: 50px;position: fixed;top: 30px;right: 30px;color: white;cursor: pointer;" onclick="closePicture(this)">×</p>' +
        '</div>';
    $('body').append(imgHtml);
}

//关闭
function closePicture(obj) {
    $(obj).parent("div").remove();
}

//图片预览路径
function getObjectURL(file) {
    let url = null;
    if (window.createObjectURL != undefined) { // basic
        url = window.createObjectURL(file);
    } else if (window.URL != undefined) { // mozilla(firefox)
        url = window.URL.createObjectURL(file);
    } else if (window.webkitURL != undefined) { // webkit or chrome
        url = window.webkitURL.createObjectURL(file);
    }
    return url;
}

// 消息弹框
(function () {
    $.MsgBox = {
        Alert: function (title, msg) {
            GenerateHtml("alert", title, msg);
            btnOk(); //alert只是弹出消息，因此没必要用到回调函数callback
            btnNo();
        },
        Confirm: function (title, msg, callback) {
            GenerateHtml("confirm", title, msg);
            btnOk(callback);
            btnNo();
        }
    };
    //生成Html
    var GenerateHtml = function (type, title, msg) {
        let html = "";
        html += '<div id="mb_box"></div><div id="mb_con"><span id="mb_tit">' + title + '</span>';
        html += '<a id="mb_ico">x</a><div id="mb_msg">' + msg + '</div><div id="mb_btnbox">';
        if (type == "alert") {
            html += '<input id="mb_btn_ok" type="button" value="确定" />';
        }
        if (type == "confirm") {
            html += '<input id="mb_btn_ok" type="button" value="确定" />';
            html += '<input id="mb_btn_no" type="button" value="取消" />';
        }
        html += '</div></div>';
        //必须先将_html添加到body，再设置Css样式
        $("body").append(html);
        //生成Css
        GenerateCss();
    };

    //生成Css
    let GenerateCss = function () {
        $("#mb_box").css({
            width: '100%',
            height: '100%',
            zIndex: '99999',
            position: 'fixed',
            filter: 'Alpha(opacity=60)',
            backgroundColor: 'black',
            top: '0',
            left: '0',
            opacity: '0.6'
        });
        $("#mb_con").css({
            zIndex: '999999',
            width: '400px',
            position: 'fixed',
            backgroundColor: 'White',
            borderRadius: '15px'
        });
        $("#mb_tit").css({
            display: 'block',
            fontSize: '14px',
            color: '#444',
            padding: '10px 15px',
            backgroundColor: '#DDD',
            borderRadius: '15px 15px 0 0',
            borderBottom: '3px solid #009BFE',
            fontWeight: 'bold'
        });
        $("#mb_msg").css({
            padding: '20px',
            lineHeight: '20px',
            borderBottom: '1px dashed #DDD',
            fontSize: '13px'
        });
        $("#mb_ico").css({
            display: 'block',
            position: 'absolute',
            right: '10px',
            top: '9px',
            border: '1px solid Gray',
            width: '18px',
            height: '18px',
            textAlign: 'center',
            lineHeight: '16px',
            cursor: 'pointer',
            borderRadius: '12px',
            fontFamily: '微软雅黑'
        });
        $("#mb_btnbox").css({
            margin: '15px 0 10px 0',
            textAlign: 'center'
        });
        $("#mb_btn_ok,#mb_btn_no").css({
            width: '85px',
            height: '30px',
            color: 'white',
            border: 'none'
        });
        $("#mb_btn_ok").css({
            backgroundColor: '#168bbb'
        });
        $("#mb_btn_no").css({
            backgroundColor: 'gray',
            marginLeft: '20px'
        });
        //右上角关闭按钮hover样式
        $("#mb_ico").hover(function () {
            $(this).css({
                backgroundColor: 'Red',
                color: 'White'
            });
        }, function () {
            $(this).css({
                backgroundColor: '#DDD',
                color: 'black'
            });
        });
        let widht = document.documentElement.clientWidth; //屏幕宽
        let height = document.documentElement.clientHeight; //屏幕高
        let boxWidth = $("#mb_con").width();
        let boxHeight = $("#mb_con").height();
        //让提示框居中
        $("#mb_con").css({
            top: (height - boxHeight) / 2 + "px",
            left: (widht - boxWidth) / 2 + "px"
        });
    };
    //确定按钮事件
    var btnOk = function (callback) {
        $("#mb_btn_ok").click(function () {
            $("#mb_box,#mb_con").remove();
            if (typeof(callback) == 'function') {
                callback();
            }
        });
    };
    //取消按钮事件
    var btnNo = function () {
        $("#mb_btn_no,#mb_ico").click(function () {
            $("#mb_box,#mb_con").remove();
        });
    }
})();