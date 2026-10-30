package hr.novashop.app

import android.os.Bundle
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import java.util.Locale

private val Orange = Color(0xFFFF6A00)
private val Ink = Color(0xFF171717)
private val Pale = Color(0xFFFFF3E9)

data class Product(val id: Int, val name: String, val category: String, val price: Double, val old: Double, val emoji: String, val stock: Int)

private val seedProducts = listOf(
    Product(1, "Bežične slušalice", "Elektronika", 12.99, 29.99, "🎧", 42),
    Product(2, "LED stolna lampa", "Dom", 16.50, 24.90, "💡", 18),
    Product(3, "Sportska boca", "Sport", 7.49, 12.99, "🥤", 65),
    Product(4, "Mini torbica", "Moda", 9.90, 19.90, "👜", 27),
    Product(5, "Pametni sat", "Elektronika", 22.90, 49.90, "⌚", 13),
    Product(6, "Dekorativni jastuk", "Dom", 8.25, 14.50, "🛋️", 31),
    Product(7, "Sunčane naočale", "Moda", 6.99, 15.99, "🕶️", 54),
    Product(8, "Podloga za vježbanje", "Sport", 14.90, 25.00, "🧘", 20)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NovaShopApp() }
    }
}

private const val PREFS_NAME = "nova_shop_local"
private const val PRODUCTS_KEY = "products_json"
private const val CART_KEY = "cart_json"

private fun loadProducts(context: Context): List<Product> {
    val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(PRODUCTS_KEY, null) ?: return seedProducts
    return try {
        val array = JSONArray(raw)
        (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            Product(o.getInt("id"), o.getString("name"), o.getString("category"),
                o.getDouble("price"), o.getDouble("old"), o.getString("emoji"), o.getInt("stock"))
        }
    } catch (_: Exception) { seedProducts }
}

private fun loadCart(context: Context): Map<Int, Int> {
    val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(CART_KEY, null) ?: return emptyMap()
    return try {
        val o = JSONObject(raw)
        o.keys().asSequence().associate { it.toInt() to o.getInt(it) }
    } catch (_: Exception) { emptyMap() }
}

private fun saveLocalState(context: Context, products: List<Product>, cart: Map<Int, Int>) {
    val productArray = JSONArray()
    products.forEach { p ->
        productArray.put(JSONObject().put("id", p.id).put("name", p.name).put("category", p.category)
            .put("price", p.price).put("old", p.old).put("emoji", p.emoji).put("stock", p.stock))
    }
    val cartObject = JSONObject()
    cart.forEach { (id, qty) -> cartObject.put(id.toString(), qty) }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        .putString(PRODUCTS_KEY, productArray.toString())
        .putString(CART_KEY, cartObject.toString())
        .apply()
}

@Composable
fun NovaShopApp() {
    val context = LocalContext.current
    var products by remember { mutableStateOf(loadProducts(context)) }
    var cart by remember { mutableStateOf(loadCart(context)) }
    SideEffect { saveLocalState(context, products, cart) }
    var page by remember { mutableStateOf("Početna") }
    var category by remember { mutableStateOf("Sve") }
    var query by remember { mutableStateOf("") }
    var adminMode by remember { mutableStateOf(false) }
    val cartCount = cart.values.sum()
    val total = products.sumOf { p -> p.price * (cart[p.id] ?: 0) }

    MaterialTheme(colorScheme = lightColorScheme(primary = Orange, onPrimary = Color.White, background = Color(0xFFF8F8F8))) {
        Scaffold(
            containerColor = Color(0xFFF8F8F8),
            topBar = {
                Column(Modifier.background(Color.White)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("NOVA", fontSize = 25.sp, fontWeight = FontWeight.Black, color = Orange)
                        Text(" SHOP", fontSize = 25.sp, fontWeight = FontWeight.Black, color = Ink)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { page = "Košarica"; adminMode = false }) {
                            BadgedBox(badge = { if (cartCount > 0) Badge { Text("$cartCount") } }) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Košarica", tint = Ink)
                            }
                        }
                        IconButton(onClick = { adminMode = !adminMode; page = if (adminMode) "Administracija" else "Početna" }) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Administracija", tint = Ink)
                        }
                    }
                    OutlinedTextField(
                        value = query, onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 10.dp).height(52.dp),
                        placeholder = { Text("Pretraži proizvode...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        singleLine = true, shape = RoundedCornerShape(14.dp)
                    )
                }
            },
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    listOf("Početna", "Kategorije", "Narudžbe", "Profil").forEach { item ->
                        NavigationBarItem(
                            selected = page == item, onClick = { page = item; adminMode = false },
                            icon = {
                                Icon(when(item) {
                                    "Početna" -> Icons.Default.Home
                                    "Kategorije" -> Icons.Default.Category
                                    "Narudžbe" -> Icons.Default.ReceiptLong
                                    else -> Icons.Default.Person
                                }, contentDescription = item)
                            },
                            label = { Text(item) }
                        )
                    }
                }
            }
        ) { padding ->
            when (page) {
                "Košarica" -> CartScreen(products, cart, total, onQty = { id, qty ->
                    cart = if (qty <= 0) cart - id else cart + (id to qty)
                }, onOrder = { page = "Narudžbe"; cart = emptyMap() }, padding = padding)
                "Administracija" -> AdminScreen(products, onAdd = {
                    products = products + Product((products.maxOfOrNull { it.id } ?: 0) + 1, "Novi proizvod", "Novo", 9.99, 14.99, "📦", 10)
                }, onDelete = { id -> products = products.filterNot { it.id == id } }, padding = padding)
                "Kategorije" -> CategoryScreen(onPick = { category = it; page = "Početna" }, padding = padding)
                "Narudžbe" -> SimpleScreen("Moje narudžbe", "Ovdje će se prikazivati potvrđene narudžbe.", Icons.Default.ReceiptLong, padding)
                "Profil" -> SimpleScreen("Moj račun", "Prijava, registracija, adrese i postavke računa.", Icons.Default.Person, padding)
                else -> HomeScreen(products.filter {
                    (category == "Sve" || it.category == category) &&
                    (query.isBlank() || it.name.contains(query, true))
                }, category, onCategory = { category = it }, onAdd = { id -> cart = cart + (id to ((cart[id] ?: 0) + 1)) }, padding = padding)
            }
        }
    }
}

@Composable
fun HomeScreen(items: List<Product>, category: String, onCategory: (String) -> Unit, onAdd: (Int) -> Unit, padding: PaddingValues) {
    Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(14.dp)) {
        Row(Modifier.fillMaxWidth().background(Orange, RoundedCornerShape(20.dp)).padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("VELIKA AKCIJA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Pronađi svoje favorite", color = Color.White, fontWeight = FontWeight.Black, fontSize = 23.sp)
                Text("Odlične cijene svaki dan", color = Color.White, fontSize = 13.sp)
            }
            Text("🛍️", fontSize = 48.sp)
        }
        Spacer(Modifier.height(18.dp))
        Text("Kategorije", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Sve", "Elektronika", "Dom", "Sport", "Moda").forEach {
                FilterChip(selected = category == it, onClick = { onCategory(it) }, label = { Text(it) })
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Popularni proizvodi", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
            Spacer(Modifier.weight(1f))
            Text("${items.size} artikala", color = Color.Gray, fontSize = 12.sp)
        }
        Spacer(Modifier.height(10.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 1800.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), userScrollEnabled = false) {
            items(items, key = { it.id }) { p -> ProductCard(p, onAdd) }
        }
        Spacer(Modifier.height(20.dp))
        Text("NOVA SHOP  •  Kupovina jednostavno.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}

@Composable
fun ProductCard(p: Product, onAdd: (Int) -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column {
            Box(Modifier.fillMaxWidth().height(125.dp).background(Pale), contentAlignment = Alignment.Center) {
                Text(p.emoji, fontSize = 58.sp)
                Text("-${(((p.old - p.price) / p.old) * 100).toInt()}%", modifier = Modifier.align(Alignment.TopStart).padding(8.dp).background(Orange, RoundedCornerShape(6.dp)).padding(horizontal = 7.dp, vertical = 3.dp), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.padding(10.dp)) {
                Text(p.name, fontWeight = FontWeight.SemiBold, color = Ink, maxLines = 1)
                Text(p.category, color = Color.Gray, fontSize = 11.sp)
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(eur(p.price), color = Orange, fontWeight = FontWeight.Black, fontSize = 17.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(eur(p.old), color = Color.Gray, fontSize = 11.sp)
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { onAdd(p.id) }, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(4.dp), shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Default.AddShoppingCart, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Dodaj", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun CartScreen(products: List<Product>, cart: Map<Int, Int>, total: Double, onQty: (Int, Int) -> Unit, onOrder: () -> Unit, padding: PaddingValues) {
    Column(Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
        Text("Košarica", fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        if (cart.isEmpty()) {
            Text("Tvoja košarica je prazna 🛒", color = Color.Gray)
        } else {
            products.filter { cart.containsKey(it.id) }.forEach { p ->
                Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(14.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(p.emoji, fontSize = 30.sp)
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Text(p.name, fontWeight = FontWeight.SemiBold)
                        Text(eur(p.price), color = Orange)
                    }
                    IconButton(onClick = { onQty(p.id, (cart[p.id] ?: 1) - 1) }) { Icon(Icons.Default.Remove, null) }
                    Text("${cart[p.id]}", fontWeight = FontWeight.Bold)
                    IconButton(onClick = { onQty(p.id, (cart[p.id] ?: 0) + 1) }) { Icon(Icons.Default.Add, null) }
                }
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.weight(1f))
            Text("Ukupno: ${eur(total)}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Button(onClick = onOrder, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text("Nastavi na narudžbu") }
            Text("Demo: narudžba se ne šalje na poslužitelj.", color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
fun AdminScreen(products: List<Product>, onAdd: () -> Unit, onDelete: (Int) -> Unit, padding: PaddingValues) {
    Column(Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
        Text("Administracija", fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("Upravljanje katalogom • demo način", color = Color.Gray)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Proizvodi", "${products.size}", Modifier.weight(1f))
            StatCard("Vrijednost zaliha", "${products.sumOf { it.stock }}", Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Add, null); Text("  Dodaj probni proizvod") }
        Spacer(Modifier.height(12.dp))
        Text("Katalog proizvoda", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Column(Modifier.verticalScroll(rememberScrollState())) {
            products.forEach { p ->
                Row(Modifier.fillMaxWidth().padding(vertical = 7.dp).background(Color.White, RoundedCornerShape(12.dp)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(p.emoji, fontSize = 26.sp)
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Text(p.name, fontWeight = FontWeight.SemiBold)
                        Text("${eur(p.price)} • Zaliha: ${p.stock}", color = Color.Gray, fontSize = 12.sp)
                    }
                    IconButton(onClick = { onDelete(p.id) }) { Icon(Icons.Default.DeleteOutline, null, tint = Color.Red) }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Zaštita administratorskog pristupa i sinkronizacija s bazom još nisu spojene.", color = Color.Gray, fontSize = 11.sp)
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Pale), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = Color.Gray, fontSize = 12.sp)
            Text(value, fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Orange)
        }
    }
}

@Composable
fun CategoryScreen(onPick: (String) -> Unit, padding: PaddingValues) {
    Column(Modifier.padding(padding).padding(16.dp)) {
        Text("Istraži kategorije", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        listOf("Elektronika" to "🎧", "Dom" to "🏠", "Sport" to "⚽", "Moda" to "👕").forEach { (name, emoji) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp).background(Color.White, RoundedCornerShape(14.dp)).clickable { onPick(name) }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 28.sp); Text(name, modifier = Modifier.weight(1f).padding(start = 12.dp), fontWeight = FontWeight.SemiBold); Icon(Icons.Default.ChevronRight, null)
            }
        }
    }
}

@Composable
fun SimpleScreen(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, padding: PaddingValues) {
    Column(Modifier.padding(padding).fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(icon, null, tint = Orange, modifier = Modifier.size(54.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Color.Gray, textAlign = TextAlign.Center)
    }
}

fun eur(value: Double): String = String.format(Locale.GERMANY, "%.2f €", value)
