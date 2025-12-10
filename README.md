# Movito – Movie Application

Movito is a modern Android movie application built using **Kotlin** and **Jetpack Compose**, following the **MVVM architecture pattern**.  
The app provides a complete entertainment browsing experience with advanced search, personalized movie lists, social features, and real-time chat.

---

##  Features

###  Search System
- Search for **movies, TV shows, and actors**
- Supports:
  - Text Search
  - Voice Search 
- Displays **recent searches**


###  Movies & TV Shows
- View full movie details:
  - Title, release date, rating, description
  - Cast and similar movies
- Movie actions:
  -  Add to Favorites
  -  Add to Watched
  -  Add to Watchlist
  -  Add personal rating
- TV Shows:
  - View full details only (no favorites, watchlist, or rating)

---

###  Actor Details
- Actor biography
- Birth date & birthplace
- Current age
- Profile image
- Full filmography


### Home Screen
- Watchlist section
- Top Rated Movies
- Coming Soon Movies
- Multiple dynamic movie categories

---

##  Social Features

###  Chat System
- Private chats between users
- Group chats
- Group admin can:
  - Add users
  - Remove users
  - Manage group settings


###  Friends System
- Search users by username
- Send friend requests
- Friend request states:
  -  Accept
  -  Decline
  -  Pending
  -  Cancel request
- View friends' profiles:
  - Favorites
  - Watched
  - Watchlist
  - Ratings


##  User Profile
- Username
- Email
- Phone number
- Avatar (auto-generated from username)
- Displays:
  - Favorites
  - Watched
  - Watchlist
  - Ratings
- Edit Profile:
  - Change username
  - Change email
  - Change phone number
  - Upload profile image


## Authentication System

###  Sign Up
- Username
- Email
- Password
- Confirm Password
- Phone number
- Show/Hide password feature
- Auto redirect to Home after successful signup

###  Login
- Login using email,phone or username & password

###  Reset Password
- Enter email
- Receive reset link
- Set new password


##  Tech Stack

- **Kotlin**
- **Jetpack Compose**
- **MVVM Architecture**
- :contentReference[oaicite:0]{index=0} (Authentication, Database, Storage)
- :contentReference[oaicite:1]{index=1} API (Movies, TV Shows, Actors)
- StateFlow / ViewModel
- REST API Integration


##  Project Structure (MVVM)

- **Model** → Data & API models  
- **View** → Jetpack Compose UI  
- **ViewModel** → Business logic & state management  


##  Functional Modules

- Authentication Module
- Home Module
- Movie Details Module
- Actor Details Module
- Search Module
- Favorites / Watched / Watchlist Module
- Rating System
- Chat Module
- Friends Module
- Profile Module


##  Testing
- Authentication testing
- Movie & Actor display testing
- Chat testing
- Friends system testing
- Search system testing
- UI/UX testing

##  Results

Movito successfully delivers:
- A complete movie browsing experience
- Personalized movie management
- Secure authentication
- Real-time private & group chat
- Friends & social interaction
- Profile customization


##  Future Enhancements

- AI-based recommendations
- Push notifications
- Dark / Light mode
- Multi-language support
- Advanced UI animations
- User activity analytics


##  Conclusion

Movito is a full-featured Android movie application that combines entertainment browsing with social interaction.  
The project follows clean architecture principles using MVVM and modern Android technologies, making it scalable and production-ready.


##  Developed By
Movito Team 💙  
