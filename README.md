# TabNews Kotlin

A Kotlin library wrapper for the TabNews API, providing a clean interface for Android applications with built-in caching and authentication management.

## Installation

```kotlin
dependencies {
    implementation("com.github.rphlfc:tabnews-kotlin:1.0.6")
}
```

Add JitPack to your project-level `build.gradle.kts`:

```kotlin
allprojects {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

## Quick Start

```kotlin
import com.github.rphlfc.tabnews_kotlin.api.APIClient

val apiClient = APIClient.Builder(context)
    .enableLogging(true)
    .build()

val contentRepository = apiClient.contentRepository
val authRepository = apiClient.authRepository
val userRepository = apiClient.userRepository
val authManager = apiClient.authManager
```

### Fetching Content

```kotlin
import com.github.rphlfc.tabnews_kotlin.model.Strategy

val result = contentRepository.getContents(page = 1, perPage = 20, strategy = Strategy.RELEVANT)

result.onSuccess { contents ->
    contents.forEach { println("Title: ${it.title}") }
}.onFailure { error ->
    println("Error: ${error.message}")
}
```

### Authentication

```kotlin
val loginResult = authRepository.login(email = "your@email.com", password = "password")

loginResult.onSuccess { response ->
    println("Token: ${response.token}")
}.onFailure { error ->
    println("Login failed: ${error.message}")
}

if (authManager.isLoggedIn()) {
    userRepository.getLoggedUser().onSuccess { user ->
        println("Welcome, ${user.username}!")
    }
}

authRepository.logout()
```

### Creating Content

```kotlin
import com.github.rphlfc.tabnews_kotlin.model.PublishStatus

contentRepository.createContent(
    title = "O título da publicação.",
    body = "O corpo da sua publicação.",
    status = PublishStatus.PUBLISHED
)
    .onSuccess { println("Content created: ${it.id}") }
    .onFailure { println("Error: ${it.message}") }
```

### Editing Content

Only the fields you pass are updated; omitted fields are left unchanged.

```kotlin
contentRepository.editContent(
    ownerUsername = "your_username",
    slug = "o-titulo-da-publicacao",
    body = "O novo corpo da publicação."
)
    .onSuccess { println("Content updated: ${it.id}") }
    .onFailure { println("Error: ${it.message}") }
```

### Updating a Profile

Only the fields you pass are updated; omitted fields are left unchanged.

```kotlin
userRepository.updateUser(
    username = "your_username",
    description = "Nova descrição do perfil.",
    notifications = true
)
    .onSuccess { println("Profile updated: ${it.username}") }
    .onFailure { println("Error: ${it.message}") }
```

## Configuration

```kotlin
val apiClient = APIClient.Builder(context)
    .baseUrl("https://www.tabnews.com.br/")
    .timeouts(connectSeconds = 30, readSeconds = 30, writeSeconds = 30)
    .enableLogging(true)
    .dispatcher(Dispatchers.IO)
    .json(Json { ignoreUnknownKeys = true; isLenient = true })
    .build()
```

## Usage Patterns

### Singleton Pattern

You can create a singleton instance of `APIClient` to use throughout your application:

```kotlin
import android.content.Context
import com.github.rphlfc.tabnews_kotlin.api.APIClient

object TabNewsClient {
    @Volatile
    private var instance: APIClient? = null
    
    fun getInstance(context: Context): APIClient {
        return instance ?: synchronized(this) {
            instance ?: APIClient.Builder(context)
                .enableLogging(true)
                .build()
                .also { instance = it }
        }
    }
}

// Usage in your Activity/Fragment
class MainActivity : AppCompatActivity() {
    private val apiClient = TabNewsClient.getInstance(this)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val contentRepository = apiClient.contentRepository
        // Use repositories...
    }
}
```

### Dependency Injection

#### Using Hilt

First, add Hilt to your project and create a module:

```kotlin
import android.content.Context
import com.github.rphlfc.tabnews_kotlin.api.APIClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TabNewsModule {
    
    @Provides
    @Singleton
    fun provideAPIClient(@ApplicationContext context: Context): APIClient {
        return APIClient.Builder(context)
            .enableLogging(true)
            .build()
    }
    
    @Provides
    fun provideContentRepository(apiClient: APIClient) = apiClient.contentRepository
    
    @Provides
    fun provideAuthRepository(apiClient: APIClient) = apiClient.authRepository
    
    @Provides
    fun provideUserRepository(apiClient: APIClient) = apiClient.userRepository
    
    @Provides
    fun provideAuthManager(apiClient: APIClient) = apiClient.authManager
}
```

Then inject it into your classes:

```kotlin
import com.github.rphlfc.tabnews_kotlin.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ContentViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {
    
    fun loadContents() {
        viewModelScope.launch {
            contentRepository.getContents(page = 1, perPage = 20)
                .onSuccess { contents ->
                    // Handle success
                }
                .onFailure { error ->
                    // Handle error
                }
        }
    }
}
```

#### Manual Dependency Injection

You can also use manual constructor injection:

```kotlin
import com.github.rphlfc.tabnews_kotlin.api.APIClient
import com.github.rphlfc.tabnews_kotlin.repository.ContentRepository

class ContentViewModel(
    private val contentRepository: ContentRepository
) : ViewModel() {
    // Use contentRepository...
}

// In your Activity/Fragment
class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: ContentViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val apiClient = APIClient.Builder(this)
            .enableLogging(true)
            .build()
        
        viewModel = ContentViewModel(apiClient.contentRepository)
    }
}
```

## API Reference

### ContentRepository
- `getContents(page, perPage, strategy, clearCache)`: Get paginated content list
- `getPostDetail(ownerUsername, slug, clearCache)`: Get post details
- `getContentsByUser(username, page, perPage, strategy, withChildren, withRoot)`: Get posts by a specific user
- `getComments(ownerUsername, slug)`: Get comments for a post
- `getContentParent(ownerUsername, slug)`: Get the parent content of a comment
- `getContentRoot(ownerUsername, slug)`: Get the root post of a thread
- `createContent(title, body, slug, sourceUrl, status)`: Create content (requires auth)
- `createComment(parent, body, status)`: Create comment (requires auth)
- `editContent(ownerUsername, slug, title, body, sourceUrl, status)`: Edit an existing post or comment; only the provided fields are updated (requires auth)
- `tabcoins(ownerUsername, slug, transactionType)`: Vote on content (requires auth)

### AuthRepository
- `login(email, password)`: Authenticate user
- `logout()`: Invalidate the session on the server and clear the local token (the local session is always cleared, even if the server request fails)

### UserRepository
- `getLoggedUser()`: Get current user profile (requires auth)
- `getUserByUsername(username)`: Get public profile by username
- `updateUser(username, newUsername, email, password, description, notifications)`: Update a user profile; only the provided fields are updated (requires auth)

### AuthManager
- `isLoggedIn()`: Check authentication status
- `getToken()`: Get current token
- `setToken(token, userId, expiresAt)`: Set token

## Requirements

- Android API 23+ (Android 6.0)
- Kotlin 1.8+

## Changelog

### 1.0.7
- Added `editContent(ownerUsername, slug, ...)` to `ContentRepository` — edits an existing post or comment via `PATCH /contents/{username}/{slug}` (only provided fields are updated)
- Added `updateUser(username, ...)` to `UserRepository` — updates a user profile via `PATCH /users/{username}` (only provided fields are updated)
- `AuthRepository.logout()` now invalidates the session on the server via `DELETE /sessions` before clearing the local token (the local session is always cleared, even if the server request fails)

### 1.0.6
- Added `getContentParent(ownerUsername, slug)` to `ContentRepository` — fetches the parent content of a comment
- Added `getContentRoot(ownerUsername, slug)` to `ContentRepository` — fetches the root post of a thread

### 1.0.5
- Added `getUserByUsername(username)` to `UserRepository` — fetches a public user profile by username

### 1.0.4
- Added `getContentsByUser(username, ...)` to `ContentRepository` — fetches paginated posts by a specific user

### 1.0.3
- Refactored API error handling: `ErrorHandler` renamed to `APIRequest` and moved to `api` package
- `APIResult` moved from `model` to `api` package
- Added `TokenProvider` interface for token management abstraction
- Improved code organization and reduced duplication

## Contributing

Contributions are welcome! Please submit a Pull Request.

## Support

For issues and questions, please open an issue on the GitHub repository.

