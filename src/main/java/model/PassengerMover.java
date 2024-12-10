package model; // 确保包名与项目结构一致

public interface PassengerMover {
    /**
     * 动画化将乘客移动到指定服务点
     * @param customer 需要移动的乘客
     * @param type 服务点的类型 (如 "CheckIn", "SecurityCheck")
     * @param index 服务点的索引 (如第几个服务点)
     */
    void movePassengerToServicePoint(Customer customer, String type, int index);




}
