# Cờ Tỉ Phú

![Java](https://img.shields.io/badge/Language-Java/C++-blue)
![Architecture](https://img.shields.io/badge/Architecture-OOP%20%7C%20Component--based-success)
![Status](https://img.shields.io/badge/Status-In%20Development-orange)

Một hệ thống lõi (Core Engine) cho game board game theo phong cách Cờ Tỷ Phú. Dự án được thiết kế theo mô hình Hướng đối tượng (OOP) với kiến trúc phân tách cao: mỗi thực thể logic được đóng gói kèm với tài nguyên đồ họa (UI/Assets) của riêng nó, giúp hệ thống dễ dàng mở rộng, bảo trì và tích hợp giao diện sau này.



##  Cấu trúc Dự án (Project Structure)

Để tối ưu hóa việc quản lý mã nguồn và tài nguyên hình ảnh/âm thanh, dự án áp dụng cấu trúc **1 Đối tượng = 1 Package**. Mỗi package sẽ tự quản lý logic và assets riêng biệt.

```text
📦 ProjectOop
┣ 📂 src
┃ ┣ 📂 game_controller         # Trung tâm điều phối hệ thống
┃ ┃ ┣ 📜 GameController.java
┃ ┃ ┗ 📂 assets                # Hình ảnh/âm thanh UI hệ thống, màn hình Win/Lose
┃ ┣ 📂 player                  # Quản lý người chơi
┃ ┃ ┣ 📜 Player.java
┃ ┃ ┗ 📂 assets                # Avatars, icon token (xe hơi, nón...)
┃ ┣ 📂 board                   # Quản lý bản đồ
┃ ┃ ┣ 📜 Board.java
┃ ┃ ┗ 📂 assets                # Ảnh nền bàn cờ (board_bg.png)
┃ ┣ 📂 square                  # Các ô trên bàn cờ
┃ ┃ ┣ 📜 Square.java           # Abstract Class
┃ ┃ ┣ 📜 PropertySquare.java   # Ô đất
┃ ┃ ┣ 📜 JailSquare.java       # Ô nhà tù
┃ ┃ ┗ 📂 assets                # Icon nhà, khách sạn, thẻ cơ hội
┃ ┗ 📂 dice                    # Bộ sinh số ngẫu nhiên
┃   ┣ 📜 Dice.java
┃   ┗ 📂 assets                # Animation/Hình ảnh 6 mặt xúc xắc (1.png -> 6.png)
