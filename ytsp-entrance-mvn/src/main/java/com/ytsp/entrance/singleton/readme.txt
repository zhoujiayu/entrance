需要进行单例控制的终端必须每隔一段时间访问SingleEndpointPingServlet，并携带cid/ckey/callbackType/callbackUri四个参数：
cid:用户id
ckey:终端在一次会话中产生的唯一标识
callbackType:当发生冲突时，本模块用以回调的方式，目前只有“http”
callbackUri:发生冲突时，回调的uri地址，本模块回调时将携带cid/ckey两个参数，目标模块接收到回调时自行处理登出逻辑

模拟效果：
终端A用账户“张三”登录，每隔一分钟发送心跳请求到SingleEndpointPingServlet，这时本模块将做记录
终端B再次用账户“张三”登录，并发送心跳请求到SingleEndpointPingServlet，
这时本模块将回调通知终端A，检测到账户在其他地点登录，终端A自行处理登出逻辑