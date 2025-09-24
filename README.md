# TabNews Kotlin

A Kotlin library wrapper for the TabNews API (https://www.tabnews.com.br), providing a clean and easy-to-use interface for Android applications with built-in caching and authentication management.

## Installation

Add the following to your `build.gradle.kts` (module level):

```kotlin
dependencies {
    implementation("com.github.rphlfc:tabnews-kotlin:1.0.0")
}
```

Add JitPack to your `build.gradle.kts` (project level):

```kotlin
allprojects {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

## Quick Start

### Basic Usage

```kotlin
import com.github.rphlfc.tabnews_kotlin.api.APIClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create API client
        val apiClient = APIClient.Builder(this)
            .enableLogging(true) // Enable for debugging
            .build()
        
        // Get repositories
        val contentRepository = apiClient.contentRepository
        val authRepository = apiClient.authRepository
        val userRepository = apiClient.userRepository
        val authManager = apiClient.authManager
        
        // Use the repositories...
    }
}
```

### Fetching Content

```kotlin
// Get content list
val result = contentRepository.getContents(
    page = 1,
    perPage = 20,
    strategy = "relevant" // or "new"
)

result.onSuccess { contents ->
    // Handle successful response
    contents.forEach { content ->
        println("Title: ${content.title}")
        println("Author: ${content.ownerUsername}")
    }
}.onFailure { error ->
    // Handle error
    println("Error: ${error.message}")
}

// Get specific post details
val postResult = contentRepository.getPostDetail(
    ownerUsername = "username",
    slug = "post-slug"
)

// Get comments for a post
val commentsResult = contentRepository.getComments(
    ownerUsername = "username",
    slug = "post-slug"
)
```

### Authentication

```kotlin
// Login
val loginResult = authRepository.login(
    email = "your@email.com",
    password = "yourpassword"
)

loginResult.onSuccess { response ->
    println("Login successful! Token: ${response.token}")
    println("User ID: ${response.id}")
}.onFailure { error ->
    println("Login failed: ${error.message}")
}

// Check if user is logged in
val isLoggedIn = authManager.isLoggedIn()

// Get current user profile (requires authentication)
if (isLoggedIn) {
    val userResult = userRepository.getLoggedUser()
    userResult.onSuccess { user ->
        println("Welcome, ${user.username}!")
    }
}

// Logout
authRepository.logout()
```

### Creating Content

```kotlin
// Create new content (requires authentication)
val contentRequest = ContentRequest(
    title = "O título da publicação.",
    body = "O corpo da sua publicação, com formatação em Markdown ou HTML.",
    status = "published", // or "draft"
    sourceUrl = "https://example.com", // optional
    slug = "o-slug-do-seu-post" // optional
)

val createResult = contentRepository.createContent(contentRequest)

createResult.onSuccess { content ->
    println("Content created successfully!")
    println("ID: ${content.id}")
    println("Title: ${content.title}")
    println("Status: ${content.status}")
}.onFailure { error ->
    println("Content creation failed: ${error.message}")
}
```

### Creating Comments

```kotlin
// Create a comment (requires authentication)
val commentRequest = CommentRequest(
    parentId = "05828f0b-8a16-41d4-8669-3962ab25ade2", // ID of the parent content
    body = "O corpo da sua publicação, com formatação em Markdown ou HTML.",
    status = "published" // or "draft"
)

val commentResult = contentRepository.createComment(commentRequest)

commentResult.onSuccess { comment ->
    println("Comment created successfully!")
    println("ID: ${comment.id}")
    println("Parent ID: ${comment.parentId}")
    println("Body: ${comment.body}")
}.onFailure { error ->
    println("Comment creation failed: ${error.message}")
}
```

### Voting on Content

```kotlin
// Vote on content (requires authentication)
val voteResult = contentRepository.tabcoins(
    ownerUsername = "username",
    slug = "post-slug",
    transactionType = TransactionType.CREDIT // or TransactionType.DEBIT
)

voteResult.onSuccess {
    println("Vote submitted successfully!")
}.onFailure { error ->
    println("Vote failed: ${error.message}")
}
```

## Dependency Injection

### Option 1: Singleton Pattern

```kotlin
object TabNewsSingleton {
    private var apiClient: APIClient? = null
    
    fun getInstance(context: Context): APIClient {
        return apiClient ?: APIClient.Builder(context)
            .enableLogging(BuildConfig.DEBUG)
            .build().also { apiClient = it }
    }
}

// Usage
class MyRepository {
    private val apiClient = TabNewsSingleton.getInstance(context)
    private val contentRepository = apiClient.contentRepository
}
```

### Option 2: Hilt Dependency Injection

First, add Hilt to your project and create a module:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object TabNewsModule {
    
    @Provides
    @Singleton
    fun provideAPIClient(@ApplicationContext context: Context): APIClient {
        return APIClient.Builder(context)
            .enableLogging(BuildConfig.DEBUG)
            .build()
    }
    
    @Provides
    fun provideContentRepository(apiClient: APIClient): ContentRepository {
        return apiClient.contentRepository
    }
    
    @Provides
    fun provideAuthRepository(apiClient: APIClient): AuthRepository {
        return apiClient.authRepository
    }
    
    @Provides
    fun provideUserRepository(apiClient: APIClient): UserRepository {
        return apiClient.userRepository
    }
    
    @Provides
    fun provideAuthManager(apiClient: APIClient): AuthManager {
        return apiClient.authManager
    }
}
```

Then inject in your classes:

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var contentRepository: ContentRepository
    
    @Inject
    lateinit var authRepository: AuthRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Use injected repositories
        lifecycleScope.launch {
            val result = contentRepository.getContents(page = 1)
            result.onSuccess { contents ->
                // Handle success
            }
        }
    }
}
```

## Configuration

The `APIClient.Builder` provides several configuration options:

```kotlin
val apiClient = APIClient.Builder(context)
    .baseUrl("https://www.tabnews.com.br/") // Custom base URL
    .timeouts(
        connectSeconds = 30,  // Connection timeout
        readSeconds = 30,     // Read timeout
        writeSeconds = 30     // Write timeout
    )
    .enableLogging(true)      // Enable HTTP logging
    .dispatcher(Dispatchers.IO) // Custom coroutine dispatcher
    .json(Json {              // Custom JSON configuration
        ignoreUnknownKeys = true
        isLenient = true
    })
    .build()
```

## API Reference

### ContentRepository

- `getContents(page, perPage, strategy, clearCache)`: Get paginated content list
- `getPostDetail(ownerUsername, slug, clearCache)`: Get specific post details
- `getComments(ownerUsername, slug)`: Get comments for a post
- `createContent(contentRequest)`: Create new content (requires authentication)
- `createComment(commentRequest)`: Create a comment (requires authentication)
- `tabcoins(ownerUsername, slug, transactionType)`: Vote on content

### AuthRepository

- `login(email, password)`: Authenticate user
- `logout()`: Clear authentication

### UserRepository

- `getLoggedUser()`: Get current user profile (requires authentication)

### AuthManager

- `isLoggedIn()`: Check authentication status
- `getToken()`: Get current authentication token
- `setToken(token, userId, expiresAt)`: Set authentication token

## Requirements

- Android API 24+ (Android 7.0)
- Kotlin 1.8+

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For issues and questions, please open an issue on the GitHub repository.

