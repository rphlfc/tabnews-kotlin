# TabNews Kotlin

A Kotlin library wrapper for the TabNews API, providing a clean interface for Android applications with built-in caching and authentication management.

## Installation

```kotlin
dependencies {
    implementation("com.github.rphlfc:tabnews-kotlin:1.0.3")
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
- `getComments(ownerUsername, slug)`: Get comments
- `createContent(contentRequest)`: Create content (requires auth)
- `createComment(commentRequest)`: Create comment (requires auth)
- `tabcoins(ownerUsername, slug, transactionType)`: Vote on content

### AuthRepository
- `login(email, password)`: Authenticate user
- `logout()`: Clear authentication

### UserRepository
- `getLoggedUser()`: Get current user profile (requires auth)

### AuthManager
- `isLoggedIn()`: Check authentication status
- `getToken()`: Get current token
- `setToken(token, userId, expiresAt)`: Set token

## Requirements

- Android API 23+ (Android 6.0)
- Kotlin 1.8+

## Changelog

### 1.0.3
- Refactored API error handling: `ErrorHandler` renamed to `APIRequest` and moved to `api` package
- `APIResult` moved from `model` to `api` package
- Added `TokenProvider` interface for token management abstraction
- Improved code organization and reduced duplication

## Contributing

Contributions are welcome! Please submit a Pull Request.

## Support

For issues and questions, please open an issue on the GitHub repository.

