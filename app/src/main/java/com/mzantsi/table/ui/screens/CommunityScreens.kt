package com.mzantsi.table.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mzantsi.table.data.CommunityData
import com.mzantsi.table.data.CommunityGroup
import com.mzantsi.table.data.CommunityPost
import com.mzantsi.table.data.Cook
import com.mzantsi.table.data.Recipe
import com.mzantsi.table.ui.components.AssetImage
import com.mzantsi.table.ui.components.CultureStyle
import com.mzantsi.table.ui.components.imagePath
import com.mzantsi.table.ui.theme.MzantsiGold
import com.mzantsi.table.ui.theme.MzantsiGreen
import com.mzantsi.table.ui.theme.MzantsiGreenDark
import com.mzantsi.table.ui.theme.MzantsiRed
import java.io.File
import java.util.Locale

data class CommunityState(
    val recipes: List<Recipe>,
    val groups: List<CommunityGroup>,
    val cooks: List<Cook>,
    val posts: List<CommunityPost>,
    val joined: Set<String>,
    val following: Set<String>,
    val myKey: String?,
    val myName: String
)

data class CommunityActions(
    val onToggleJoin: (String) -> Unit,
    val onToggleFollow: (String) -> Unit,
    val onShare: (recipe: Recipe, communityId: String, caption: String, photoPath: String) -> Unit,
    val onRate: (CommunityPost, Int) -> Unit,
    val onCheer: (CommunityPost) -> Unit,
    val onEncourage: (CommunityPost, String) -> Unit,
    val onRecipeClick: (Recipe) -> Unit,
    val onCommunityClick: (String) -> Unit,
    val onCookClick: (String) -> Unit
)

private fun CommunityState.recipesBy(cook: String) = recipes.filter { it.authorName.equals(cook, ignoreCase = true) }
private fun CommunityState.membersOf(g: CommunityGroup) = g.baseMembers + if (g.id in joined) 1 else 0
private fun CommunityState.followersOf(c: Cook) = c.baseFollowers + if (c.name in following) 1 else 0
private fun CommunityState.group(id: String) = groups.firstOrNull { it.id == id }

@Composable
fun CommunityHubScreen(
    state: CommunityState,
    actions: CommunityActions,
    shareRecipeId: Int? = null,
    onBack: () -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showShare by remember { mutableStateOf(shareRecipeId != null) }

    Column(Modifier.fillMaxSize()) {
        CommunityHeader(
            title = "Community Hub",
            subtitle = "Join communities, follow cooks and cheer each other on",
            onBack = onBack
        )
        TabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.surface, contentColor = MzantsiGreen) {
            listOf("Feed", "Communities", "Cooks").forEachIndexed { i, label ->
                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(label) })
            }
        }
        when (tab) {
            0 -> FeedTab(state, actions, onShareClick = { showShare = true })
            1 -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Join a community to see its members' attempts in your feed.",
                        style = MaterialTheme.typography.bodyMedium, color = Color.Gray
                    )
                }
                items(state.groups, key = { it.id }) { g -> CommunityRow(g, state, actions) }
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "Follow the cooks who inspire you. Their recipes and attempts show up in your feed.",
                        style = MaterialTheme.typography.bodyMedium, color = Color.Gray
                    )
                }
                items(state.cooks, key = { it.name }) { c -> CookRow(c, state, actions) }
            }
        }
    }

    if (showShare) {
        ShareAttemptDialog(
            state = state,
            initialRecipeId = shareRecipeId,
            initialCommunityId = null,
            onDismiss = { showShare = false },
            onShare = { r, g, caption, photo ->
                actions.onShare(r, g, caption, photo)
                showShare = false
                tab = 0
            }
        )
    }
}

@Composable
private fun FeedTab(state: CommunityState, actions: CommunityActions, onShareClick: () -> Unit) {
    var onlyMine by rememberSaveable { mutableStateOf(false) }
    val posts = if (!onlyMine) state.posts else state.posts.filter {
        it.communityId in state.joined || it.authorName in state.following || it.authorKey == state.myKey
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Button(
                onClick = onShareClick,
                colors = ButtonDefaults.buttonColors(containerColor = MzantsiGreen),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Share your attempt")
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !onlyMine, onClick = { onlyMine = false }, label = { Text("Everyone") })
                FilterChip(selected = onlyMine, onClick = { onlyMine = true }, label = { Text("My communities & cooks") })
            }
        }
        if (posts.isEmpty()) {
            item {
                Text(
                    if (onlyMine) "Nothing here yet. Join a community or follow a cook to fill your feed."
                    else "No attempts yet. Cook something and be the first to share it!",
                    style = MaterialTheme.typography.bodyMedium, color = Color.Gray
                )
            }
        }
        items(posts, key = { it.postId }) { post -> PostCard(post, state, actions) }
    }
}

@Composable
fun CommunityDetailScreen(
    communityId: String,
    state: CommunityState,
    actions: CommunityActions,
    onBack: () -> Unit
) {
    val group = state.group(communityId)
    if (group == null) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            TextButton(onClick = onBack) { Text("← Back") }
            Text("This community could not be found.")
        }
        return
    }
    val joined = group.id in state.joined
    val recipes = state.recipes.filter { it.culture.equals(group.culture, ignoreCase = true) }
    val members = state.cooks.filter { it.culture.equals(group.culture, ignoreCase = true) }
    val posts = state.posts.filter { it.communityId == group.id }
    var showShare by remember { mutableStateOf(false) }
    val paths = CultureStyle.coverPaths(group.culture, state.recipes)

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Box(Modifier.fillMaxWidth().height(190.dp).background(CultureStyle.color(group.culture))) {
                AssetImage(paths.first(), group.name, Modifier.matchParentSize(), fallbackPaths = paths.drop(1))
                Box(Modifier.matchParentSize().background(CultureStyle.color(group.culture).copy(alpha = 0.55f)))
                TextButton(onClick = onBack, modifier = Modifier.padding(8.dp)) { Text("← Back", color = Color.White) }
                Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                    Text(group.name, color = Color.White, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "${state.membersOf(group)} members · ${posts.size} attempt${if (posts.size == 1) "" else "s"}",
                        color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
        item {
            Column(Modifier.padding(16.dp)) {
                Text(group.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    JoinButton(joined, Modifier.weight(1f)) { actions.onToggleJoin(group.id) }
                    OutlinedButton(onClick = { showShare = true }, shape = RoundedCornerShape(20.dp), modifier = Modifier.weight(1f)) {
                        Text("Share attempt")
                    }
                }
            }
        }
        if (members.isNotEmpty()) {
            item { SectionLabel("Cooks in this community") }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(members, key = { it.name }) { c ->
                        CommunityCookCard(c, state.recipes, Modifier.width(150.dp)) { actions.onCookClick(c.name) }
                    }
                }
            }
        }
        if (recipes.isNotEmpty()) {
            item { SectionLabel("${group.culture} recipes to try") }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(recipes, key = { it.recipeId }) { r -> RecipeCard(r) { actions.onRecipeClick(r) } }
                }
            }
        }
        item { SectionLabel("Members' attempts") }
        if (posts.isEmpty()) {
            item {
                Text(
                    "No attempts yet. Cook one of these recipes and share it to motivate the others!",
                    color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
        items(posts, key = { it.postId }) { p ->
            Box(Modifier.padding(horizontal = 16.dp, vertical = 7.dp)) { PostCard(p, state, actions) }
        }
    }

    if (showShare) {
        ShareAttemptDialog(
            state = state,
            initialRecipeId = recipes.firstOrNull()?.recipeId,
            initialCommunityId = group.id,
            onDismiss = { showShare = false },
            onShare = { r, g, caption, photo ->
                actions.onShare(r, g, caption, photo)
                showShare = false
            }
        )
    }
}

@Composable
fun CookProfileScreen(
    cookName: String,
    state: CommunityState,
    actions: CommunityActions,
    onBack: () -> Unit
) {
    val cook = state.cooks.firstOrNull { it.name == cookName }
        ?: Cook(cookName, "Other", "Shares recipes on The Mzantsi Table.", 0)
    val recipes = state.recipesBy(cook.name)
    val posts = state.posts.filter { it.authorName.equals(cook.name, ignoreCase = true) }
    val following = cook.name in state.following
    val ratingsOnAttempts = posts.flatMap { it.ratings.values }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Column(
                Modifier.fillMaxWidth().background(MzantsiGreenDark).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) { Text("← Back", color = Color.White) }
                Avatar(cook.name, CultureStyle.color(cook.culture), 72)
                Spacer(Modifier.height(8.dp))
                Text(cook.name, color = Color.White, style = MaterialTheme.typography.headlineMedium)
                Text(cook.culture, color = MzantsiGold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(cook.bio, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Stat("${recipes.size}", "recipes")
                    Stat("${state.followersOf(cook)}", "followers")
                    Stat(
                        if (ratingsOnAttempts.isEmpty()) "-" else String.format(Locale.US, "%.1f", ratingsOnAttempts.average()),
                        "attempt rating"
                    )
                }
                Spacer(Modifier.height(12.dp))
                FollowButton(following, Modifier.fillMaxWidth(0.6f)) { actions.onToggleFollow(cook.name) }
            }
        }
        item { SectionLabel("Recipes by ${cook.name}") }
        if (recipes.isEmpty()) {
            item { Text("No recipes yet.", color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp)) }
        } else {
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(recipes, key = { it.recipeId }) { r -> RecipeCard(r) { actions.onRecipeClick(r) } }
                }
            }
        }
        item { SectionLabel("Attempts shared") }
        if (posts.isEmpty()) {
            item { Text("No attempts shared yet.", color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp)) }
        }
        items(posts, key = { it.postId }) { p ->
            Box(Modifier.padding(horizontal = 16.dp, vertical = 7.dp)) { PostCard(p, state, actions) }
        }
    }
}

@Composable
fun CommunityCookCard(cook: Cook, recipes: List<Recipe>, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val theirRecipes = recipes.filter { it.authorName.equals(cook.name, ignoreCase = true) }
    val paths = (theirRecipes.map { it.imagePath() } + CultureStyle.coverPaths(cook.culture, recipes)).distinct()
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onClick() }
    ) {
        AssetImage(
            path = paths.first(),
            fallbackPaths = paths.drop(1),
            contentDescription = cook.name,
            modifier = Modifier.fillMaxWidth().height(100.dp).background(CultureStyle.color(cook.culture))
        )
        Column(Modifier.padding(12.dp)) {
            Text(cook.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(cook.culture, color = CultureStyle.color(cook.culture), fontWeight = FontWeight.SemiBold)
            Text(
                "${theirRecipes.size} recipe${if (theirRecipes.size == 1) "" else "s"}",
                color = Color.Gray, style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun CommunityRow(group: CommunityGroup, state: CommunityState, actions: CommunityActions) {
    val joined = group.id in state.joined
    val tint = CultureStyle.color(group.culture)
    val paths = CultureStyle.coverPaths(group.culture, state.recipes)
    val attempts = state.posts.count { it.communityId == group.id }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { actions.onCommunityClick(group.id) }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AssetImage(
            paths.first(), group.name,
            Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)).background(tint),
            fallbackPaths = paths.drop(1)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(group.name, style = MaterialTheme.typography.titleMedium, color = tint)
            Text(group.description, style = MaterialTheme.typography.labelSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                "${state.membersOf(group)} members · $attempts attempt${if (attempts == 1) "" else "s"}",
                style = MaterialTheme.typography.labelSmall, color = Color.Gray
            )
        }
        Spacer(Modifier.width(8.dp))
        JoinButton(joined) { actions.onToggleJoin(group.id) }
    }
}

@Composable
private fun CookRow(cook: Cook, state: CommunityState, actions: CommunityActions) {
    val following = cook.name in state.following
    val count = state.recipesBy(cook.name).size
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { actions.onCookClick(cook.name) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(cook.name, CultureStyle.color(cook.culture), 48)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(cook.name, style = MaterialTheme.typography.titleMedium)
            Text(cook.culture, color = CultureStyle.color(cook.culture), style = MaterialTheme.typography.labelSmall)
            Text(
                "$count recipe${if (count == 1) "" else "s"} · ${state.followersOf(cook)} followers",
                style = MaterialTheme.typography.labelSmall, color = Color.Gray
            )
        }
        FollowButton(following) { actions.onToggleFollow(cook.name) }
    }
}

@Composable
private fun PostCard(post: CommunityPost, state: CommunityState, actions: CommunityActions) {
    val group = state.group(post.communityId)
    val recipe = state.recipes.firstOrNull { it.recipeId == post.recipeId }
    val isMine = post.authorKey == state.myKey
    val myRating = state.myKey?.let { post.ratings[it] } ?: 0
    val cheered = state.myKey != null && state.myKey in post.cheers
    var showEncourage by remember { mutableStateOf(false) }
    val tint = CultureStyle.color(group?.culture ?: recipe?.culture ?: "")
    val photo = post.photoPath.ifBlank { recipe?.imagePath() ?: "" }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.clickable { actions.onCookClick(post.authorName) }) { Avatar(post.authorName, tint, 40) }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    (if (isMine) "You" else post.authorName) + " cooked " + post.recipeTitle,
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold
                )
                Text(
                    listOfNotNull(group?.name, timeAgo(post.createdAtMillis)).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall, color = Color.Gray,
                    modifier = Modifier.clickable { group?.let { actions.onCommunityClick(it.id) } }
                )
            }
        }
        if (photo.isNotBlank()) {
            AssetImage(
                path = photo,
                contentDescription = post.recipeTitle,
                fallbackPaths = listOfNotNull(recipe?.imagePath()),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(tint.copy(alpha = 0.25f))
                    .clickable { recipe?.let(actions.onRecipeClick) }
            )
        }
        Column(Modifier.padding(12.dp)) {
            if (post.caption.isNotBlank()) Text(post.caption, style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = MzantsiGold, modifier = Modifier.size(16.dp))
                Text(
                    if (post.ratings.isEmpty()) " Not rated yet"
                    else " " + String.format(Locale.US, "%.1f", post.averageRating) + " (${post.ratings.size})",
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(Modifier.weight(1f))
                if (!isMine) {
                    Text("Rate: ", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    (1..5).forEach { n ->
                        Icon(
                            imageVector = if (n <= myRating) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = "Rate $n star${if (n == 1) "" else "s"}",
                            tint = MzantsiGold,
                            modifier = Modifier.size(26.dp).clip(CircleShape).clickable { actions.onRate(post, n) }.padding(2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val cheerColor = if (cheered) MzantsiRed else Color.Gray
                TextButton(onClick = { actions.onCheer(post) }, enabled = !isMine) {
                    Icon(Icons.Filled.Celebration, contentDescription = null, tint = cheerColor, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        (if (cheered) "Cheered" else "Cheer") + if (post.cheers.isNotEmpty()) " · ${post.cheers.size}" else "",
                        color = cheerColor
                    )
                }
                TextButton(onClick = { showEncourage = true }) {
                    Icon(Icons.Filled.ChatBubbleOutline, contentDescription = null, tint = MzantsiGreen, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (isMine) "Reply" else "Encourage", color = MzantsiGreen)
                }
            }
            post.comments.takeLast(3).forEach { c ->
                Text(
                    buildString { append(c.authorName); append(": "); append(c.text) },
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (post.comments.size > 3) {
                Text("+ ${post.comments.size - 3} more", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }

    if (showEncourage) {
        EncourageDialog(
            authorName = post.authorName,
            onDismiss = { showEncourage = false },
            onSend = { text ->
                actions.onEncourage(post, text)
                showEncourage = false
            }
        )
    }
}

@Composable
private fun ShareAttemptDialog(
    state: CommunityState,
    initialRecipeId: Int?,
    initialCommunityId: String?,
    onDismiss: () -> Unit,
    onShare: (Recipe, String, String, String) -> Unit
) {
    val context = LocalContext.current
    var recipe by remember { mutableStateOf(state.recipes.firstOrNull { it.recipeId == initialRecipeId }) }
    var communityId by remember {
        mutableStateOf(initialCommunityId ?: recipe?.let { CommunityData.groupIdFor(it.culture) }?.takeIf { id -> state.groups.any { it.id == id } })
    }
    var caption by remember { mutableStateOf("") }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var recipeMenu by remember { mutableStateOf(false) }
    var groupMenu by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val dest = File(context.filesDir, "attempt_${System.currentTimeMillis()}.jpg")
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { input -> dest.outputStream().use { input.copyTo(it) } }
            }
            if (dest.exists() && dest.length() > 0) photoPath = dest.absolutePath
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share your attempt") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("What did you cook?", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Box {
                    OutlinedButton(onClick = { recipeMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(recipe?.title ?: "Choose a recipe", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    DropdownMenu(expanded = recipeMenu, onDismissRequest = { recipeMenu = false }) {
                        state.recipes.forEach { r ->
                            DropdownMenuItem(text = { Text(r.title) }, onClick = {
                                recipe = r
                                if (initialCommunityId == null) {
                                    val id = CommunityData.groupIdFor(r.culture)
                                    if (state.groups.any { it.id == id }) communityId = id
                                }
                                recipeMenu = false
                                error = null
                            })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Share with", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Box {
                    OutlinedButton(onClick = { groupMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(state.group(communityId.orEmpty())?.name ?: "Choose a community")
                    }
                    DropdownMenu(expanded = groupMenu, onDismissRequest = { groupMenu = false }) {
                        state.groups.forEach { g ->
                            DropdownMenuItem(text = { Text(g.name) }, onClick = { communityId = g.id; groupMenu = false; error = null })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = caption,
                    onValueChange = { if (it.length <= 280) caption = it },
                    label = { Text("How did it go?") },
                    placeholder = { Text("e.g. My first try - next time more spice!") },
                    supportingText = { Text("${caption.length}/280") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(onClick = { photoPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (photoPath == null) "Add a photo (optional)" else "Change photo")
                }
                photoPath?.let {
                    Spacer(Modifier.height(6.dp))
                    AssetImage("file:$it", "Your photo", Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(10.dp)))
                }
                error?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, color = MzantsiRed, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val r = recipe
                val g = communityId
                error = when {
                    r == null -> "Please choose the recipe you cooked."
                    g == null -> "Please choose a community to share with."
                    caption.isBlank() && photoPath == null -> "Add a short note or a photo so others can cheer you on."
                    else -> null
                }
                if (error == null && r != null && g != null) onShare(r, g, caption, photoPath?.let { "file:$it" }.orEmpty())
            }) { Text("Share") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun EncourageDialog(authorName: String, onDismiss: () -> Unit, onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Encourage $authorName") },
        text = {
            Column {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CommunityData.encouragements.forEach { msg ->
                        AssistChip(onClick = { text = msg }, label = { Text(msg, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= 280) text = it },
                    label = { Text("Your message") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { TextButton(enabled = text.isNotBlank(), onClick = { onSend(text) }) { Text("Send") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun CommunityHeader(title: String, subtitle: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(MzantsiGreenDark).padding(horizontal = 8.dp, vertical = 8.dp)) {
        TextButton(onClick = onBack) { Text("← Back", color = Color.White) }
        Column(Modifier.padding(horizontal = 8.dp)) {
            Text(title, color = Color.White, style = MaterialTheme.typography.headlineMedium)
            Text(subtitle, color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun Avatar(name: String, color: Color, sizeDp: Int) {
    Box(
        Modifier.size(sizeDp.dp).clip(CircleShape).background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            name.trim().firstOrNull()?.uppercase() ?: "?",
            color = Color.White,
            style = if (sizeDp >= 64) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, style = MaterialTheme.typography.titleMedium)
        Text(label, color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun JoinButton(joined: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (joined) {
        OutlinedButton(onClick = onClick, shape = RoundedCornerShape(20.dp), modifier = modifier) {
            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Joined")
        }
    } else {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = MzantsiGreen),
            shape = RoundedCornerShape(20.dp),
            modifier = modifier
        ) { Text("Join") }
    }
}

@Composable
private fun FollowButton(following: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (following) {
        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
            modifier = modifier
        ) { Text("Following") }
    } else {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = MzantsiGold, contentColor = Color.Black),
            shape = RoundedCornerShape(20.dp),
            modifier = modifier
        ) { Text("Follow") }
    }
}

private fun timeAgo(millis: Long): String {
    val minutes = ((System.currentTimeMillis() - millis) / 60_000L).coerceAtLeast(0)
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 60 * 24 -> "${minutes / 60}h ago"
        else -> "${minutes / (60 * 24)}d ago"
    }
}
