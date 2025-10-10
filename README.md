🃏 MyOwnPlanningPoker

📌 This README is available in English and Polish.
👉 [Jump to Polish version 🇵🇱](#-myownplanningpoker---polska-wersja)

🇬🇧 English Version

Agile Planning Poker – a full-stack application for real-time story point estimation.
The backend is built with Spring Boot (Java), and the frontend is a lightweight HTML/JS client communicating via STOMP over WebSocket.

🚀 Features  
- Create or join a planning room
- Vote using story point cards
- Reveal or reset votes in real time
- Automatic average calculation
- Live view of participants and their votes

🧱 Architecture
MyOwnPlanningPoker/  
├── src/main/java/com/kkmalysa/myownplanningpoker   # Backend  
│   ├── controller/  – STOMP handling  
│   ├── model/       – Room, Player  
│   └── dto/         – WebSocketMessage  
├── frontend/       # Static HTML/JS frontend  
└── pom.xml

Backend:  
Spring Boot + STOMP (SockJS) – manages rooms, votes, and sessions.  

Frontend:  
static HTML/JS page subscribing to /topic/room/{roomId} and /topic/{roomId}/result.  

📡 WebSocket API  
Direction	Endpoint	Description  
Client → Server	/app/join/{roomId}	Join a planning room  
Client → Server	/app/poker	Send vote, reveal, or reset actions  
Server → Client	/topic/room/{roomId}	Room state updates  
Server → Client	/topic/{roomId}/result	Average voting result  

🧪 Running Locally  
- Backend:  
mvn spring-boot:run  
Available at: http://localhost:8080  

- Frontend:  
Simply open frontend/index.html in your browser.  

📜 Author  

👤 **Karol Małysa**    
🔗 [LinkedIn](https://www.linkedin.com/in/karol-malysa/)
|  [GitHub](https://github.com/KKMalysa/MyOwnPlanningPoker)

## 🇵🇱 MyOwnPlanningPoker – Polska wersja

Agile Planning Poker – aplikacja full-stack do estymacji zadań w czasie rzeczywistym.
Backend napisany w Spring Boot (Java), frontend to lekki HTML/JS klient komunikujący się przez STOMP over WebSocket.

🚀 Funkcjonalności  
- Tworzenie i dołączanie do pokoju
- Głosowanie za pomocą kart Story Points
- Odkrywanie i resetowanie głosów w czasie rzeczywistym
- Automatyczne liczenie średniej
- Widok uczestników i ich głosów

🧱 Architektura  
MyOwnPlanningPoker/  
├── src/main/java/com/kkmalysa/myownplanningpoker   # Backend  
│   ├── controller/  – obsługa STOMP  
│   ├── model/       – Room, Player  
│   └── dto/         – WebSocketMessage  
├── frontend/       # statyczny frontend HTML/JS  
└── pom.xml  

Backend:  
Spring Boot + STOMP (SockJS), zarządza pokojami, głosami i sesjami.

Frontend:  
statyczna strona z JS, subskrybuje /topic/room/{roomId} i /topic/{roomId}/result.

📡 API WebSocket  
Kierunek	Endpoint	Opis  
Client → Server	/app/join/{roomId}	Dołączenie do pokoju  
Client → Server	/app/poker	Głosowanie, odkrywanie, reset  
Server → Client	/topic/room/{roomId}	Aktualizacja stanu pokoju  
Server → Client	/topic/{roomId}/result	Wynik z obliczoną średnią  

🧪 Uruchomienie lokalne  
Backend:  
mvn spring-boot:run  
Dostępne pod adresem: http://localhost:8080  


Frontend:  
Po prostu otwórz frontend/index.html w przeglądarce.

📜 Autor

👤 **Karol Małysa**  
🔗 [LinkedIn](https://www.linkedin.com/in/karol-malysa/) 
|  [GitHub](https://github.com/KKMalysa/MyOwnPlanningPoker)
