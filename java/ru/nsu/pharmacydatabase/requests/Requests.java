package ru.nsu.pharmacydatabase.requests;

public enum Requests {
    r1(new Request(
            "1. Получить сведения о покупателях, которые не пришли забрать \n" +
                    "свой заказ в назначенное им время и общее их число.",
            "/ru/nsu/pharmacydatabase/windows/requests/r1.fxml")),//ReceiveController
    r2(new Request(
            "2. Получить перечень и общее число покупателей, которые ждут прибытия\n" +
                    "на склад нужных им медикаментов в целом и по указанной категории медикаментов.",
            "/ru/nsu/pharmacydatabase/windows/requests/r2.fxml")),// PatientsWaitingToOrder
    r3(new Request(
            "3. Получить перечень десяти наиболее часто используемых медикаментов\n" +
                    "в целом и указанной категории медикаментов. ",
            "/ru/nsu/pharmacydatabase/windows/requests/r3.fxml")), // PopularMedicament
    r4(new Request(
            "4. Получить какой объем указанных веществ использован за указанный период.",
            "/ru/nsu/pharmacydatabase/windows/requests/r4.fxml")), // SoldMedicinesController
    r5(new Request(
            "5. Получить перечень и общее число покупателей, заказывавших определенное\n" +
            "лекарство или определенные типы лекарств за данный период",
            "/ru/nsu/pharmacydatabase/windows/requests/r5.fxml")), // BuyerListController
    r6(new Request(
            "6. Получить перечень и типы лекарств, достигших своей критической \n" +
            "нормы или закончившихся.",
            "/ru/nsu/pharmacydatabase/windows/requests/r6.fxml")),
    r7(new Request(
            "7. Получить перечень лекарств с минимальным запасом на складе в целом\n" +
            "и по указанной категории медикаментов. ",
            "/ru/nsu/pharmacydatabase/windows/requests/r7.fxml")),
    r8(new Request(
            "8. Получить полный перечень и общее число заказов, находящихся в производстве.",
            "/ru/nsu/pharmacydatabase/windows/requests/r8.fxml")),
    r9(new Request(
            "9. Получить полный перечень и общее число препаратов, требующихся \n" +
            "для заказов, находящихся в производстве.",
            "/ru/nsu/pharmacydatabase/windows/requests/r9.fxml")),
    r10(new Request(
            "10. Получить все технологии приготовления лекарств указанных типов, \n" +
            "конкретных лекарств, лекарств, находящихся в справочнике заказов в производстве.",
            "/ru/nsu/pharmacydatabase/windows/requests/r10.fxml")),
    r11(new Request(
            "11. Получить сведения о ценах на указанное лекарство, об объеме.",
            "/ru/nsu/pharmacydatabase/windows/requests/r11.fxml")),
    r12(new Request(
            "12. Получить сведения о наиболее часто делающих заказы клиентах на медикаменты \n" +
            "определенного типа, на конкретные медикаменты.",
            "/ru/nsu/pharmacydatabase/windows/requests/r12.fxml")),
    r13(new Request(
            "13. Получить сведения о конкретном лекарстве (его тип, способ приготовления, \n" +
            "цены, его количество на складе).",
            "/ru/nsu/pharmacydatabase/windows/requests/r13.fxml"));

    private final Request request;

    Requests(Request request) {
        this.request = request;
    }

    public String getWindowName() {
        return request.getWindowName();
    }

    public String getName() {
        return request.getName();
    }

    public static Requests getRequestByName(String name) {
        for (Requests req: values()) {
            if (req.getName().equals(name)) {
                return req;
            }
        }
        throw new IllegalArgumentException("No enum found with name: [" + name + "]");
    }
}
