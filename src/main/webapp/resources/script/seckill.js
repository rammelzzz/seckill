//存放主要交互逻辑js代码
//javascript 模块化
//seckill.detail.init(params)
var seckill = {
    //封装秒杀相关ajax的url
    url : {
        now : function () {
            return "http://localhost:8080/seckill/time/now";
        },
        exposer : function(seckillId) {
            return "http://localhost:8080/seckill/"+seckillId+"/exposer";
        },
        execution : function(seckillId, md5) {
            return "http://localhost:8080/seckill/" + seckillId + '/' + md5 + '/execution';
        }
    },
    handleSeckill : function(seckillId, node) {
        node.hide()
            .html('<button class="btn btn-primary btn-lg" id="killBtn">开始秒杀</button>');
        $.post(seckill.url.exposer(seckillId), {}, function(result) {
            //在回调逻辑函数中执行交互流程
            if(result && result['success']) {
                var exposer = result['data'];
                var md5 = exposer['md5'];
                if(exposer['exposed']) {
                    //开启秒杀
                    //获取秒杀的地址
                    var killUrl = seckill.url.execution(seckillId, md5);
                    console.log('killUrl='+killUrl);
                    $('#killBtn').one('click', function() {
                        //1.先禁用按钮
                        $(this).addClass('disabled');
                        $.post(killUrl, {}, function(result) {
                            if(result && result['success']) {
                                var killResult = result['data'];
                                var state = killResult['state'];
                                var stateInfo = killResult['stateInfo'];
                                //显示秒杀结果
                                node.html('<span class="label label-success">' + stateInfo + '</span>')
                            }
                        });
                    });
                    node.show();
                } else {
                    //未开启秒杀
                    var now = exposer['now'];
                    var start = exposer['start'];
                    var end = exposer['end'];
                    seckill.count(seckillId, now, start, end);
                }
            } else {
                console.log('result'+result);
            }
        });
    },
    validatePhone : function(phone) {
       if(phone  && phone.length == 11 && !isNaN(phone)) {
           return true;
       } else {
           return false;
       }
    },
    count : function(seckillId, nowTime, startTime, endTime) {
        var seckillBox = $('#seckill-box');
        if(nowTime > endTime) {
            //秒杀结束
            seckillBox.html('秒杀结束!');
        } else if(nowTime < startTime) {
            //秒杀未开启, 计时时间绑定
            var killTime = new Date(startTime + 1000);
            console.log("killTime="+killTime); //TODO
            seckillBox.countdown(killTime, function(event) {
                //控制时间格式
                var format = event.strftime('秒杀倒计时：%D天 %H时 %M分 %S秒');
                seckillBox.html(format);
            }).on('finish.countdown', function() {
                //获取秒杀地址，控制实现逻辑执行秒杀
                seckill.handleSeckill(seckillId, seckillBox);
            });
        } else {
            //秒杀开始
            seckill.handleSeckill(seckillId, seckillBox);
        }
    },
    //详情页秒杀逻辑
    detail : {
        //详情页初始化
        init : function(params) {
            //用户手机验证和登录,计时交互
            //规划交互流程
            //在cookie当中查找手机号
            var killPhone = $.cookie('killPhone');
            //验证手机号
            if(!seckill.validatePhone(killPhone)) {
                //绑定phone
                //控制输出
                var killPhoneModal = $('#killPhoneModal');
                //显示弹出层
                killPhoneModal.modal({
                    show : true, //显示弹出层
                    //禁止位置关闭
                    backdrop : 'static',
                    keyboard : false //关闭键盘时间
                });
                $('#killPhoneBtn').click(function() {
                    var inputPhone = $('#killphoneKey').val();
                    console.log('inputPhone = ' + inputPhone);  //TODO
                    if(seckill.validatePhone(inputPhone)) {
                        //电话写入cookie
                        $.cookie('killPhone', inputPhone, {expires : 7, path : '/seckill'});
                        //刷新页面
                        window.location.reload();
                    } else {
                        $('#killphoneMessage').hide().html('<label class="label label-danger">手机号错误!</label>').show(300);
                    }
                });
            }

            var startTime = params['startTime'];
            var endTime = params['endTime'];
            var seckillId = params['seckillId'];
            //已经登录
            //计时交互
            $.get(seckill.url.now(), {}, function(result) {
                if(result && result['success']) {
                    var nowTime = result['data'];
                    //计时服务的时间判断
                    seckill.count(seckillId, nowTime, startTime, endTime);
                } else {
                    console.log('result:'+result)
                }
            });
        }
    }
};