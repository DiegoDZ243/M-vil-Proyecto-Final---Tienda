package com.example.proyectofinal.ui.menu

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.proyectofinal.R
import com.example.proyectofinal.data.model.Producto
import com.example.proyectofinal.ui.TiendaViewModel


enum class PrecioFiltro(val label: String) {
    TODOS("Todos"),
    MENOR_PRECIO("Menor precio"),
    MAYOR_PRECIO("Mayor precio"),
    MENOS_DE_500("Menos de $500")
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    viewModel: TiendaViewModel,
    onCartClick: () -> Unit,
    onOpenMenu: () -> Unit,
    onProductClick: (Int) -> Unit
) {

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedPriceFilter by rememberSaveable { mutableStateOf(PrecioFiltro.TODOS) }

    //Filtro
    val filteredProducts = remember(uiState.productos, searchQuery, selectedPriceFilter) {
        val baseList = uiState.productos.filter { producto ->
            val matchesSearch = producto.activo &&
                    producto.nombre.contains(searchQuery.trim(), ignoreCase = true)
            val matchesPriceRange = when (selectedPriceFilter) {
                PrecioFiltro.MENOS_DE_500 -> producto.precio < 500.0
                else -> true
            }
            matchesSearch && matchesPriceRange
        }

        when (selectedPriceFilter) {
            PrecioFiltro.MENOR_PRECIO -> baseList.sortedBy { it.precio }
            PrecioFiltro.MAYOR_PRECIO -> baseList.sortedByDescending { it.precio }
            else -> baseList
        }
    }

    val listState = rememberLazyListState()
    // Esto permite que al hacer busquedas o filtrar, se deslice automaticamente al primer Item
    LaunchedEffect(selectedPriceFilter, searchQuery) {
        if (filteredProducts.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(uiState.carritoError) {
        if (uiState.carritoError != null) {
            snackbarHostState.showSnackbar(
                message = uiState.carritoError!!,
                actionLabel = "OK"
            )
            viewModel.limpiarErrorCarrito()
        }
    }

    Scaffold(
        topBar = {
            MenuScreenTopAppBar(
                cartCount = uiState.shoppingCart.sumOf { it.cantidad },
                onMenuClick = onOpenMenu,
                onCartClick = onCartClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(50.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                SearchBarField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
                )
                PriceFilterRow(
                    selectedFilter = selectedPriceFilter,
                    onFilterSelected = { selectedPriceFilter = it },
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                if (filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank() && selectedPriceFilter == PrecioFiltro.TODOS)
                                "No hay productos disponibles"
                            else
                                "No se encontraron productos con estos filtros",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        state=listState,
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(
                            items = filteredProducts,
                            key = { it.id }
                        ) { producto ->
                            ProductItem(
                                product = producto,
                                modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)),
                                onAgregarCarrito = { viewModel.agregarAlCarrito(producto) },
                                onProductClick = { onProductClick(producto.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreenTopAppBar(
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit,
    cartCount: Int,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = "Abrir menú")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    modifier = Modifier.size(64.dp).padding(8.dp),
                    painter = painterResource(R.drawable.tiend_logo),
                    contentDescription = null
                )
                Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
            }
        },
        actions = {
            IconButton(onClick = onCartClick) {
                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            Badge {
                                Text(text = cartCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Carrito"
                    )
                }
            }
        },
        modifier = modifier
    )
}

@Composable
fun ProductInformation(
    productName: String,
    productPrice: Double,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = productName, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small)))
        Text(text = "$%.2f".format(productPrice), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ProductImage(
    productImage: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = productImage,
        contentDescription = null,
        modifier = modifier
            .fillMaxWidth()
            .size(180.dp)
            .clip(MaterialTheme.shapes.small),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun ProductItem(
    product: Producto,
    modifier: Modifier = Modifier,
    onAgregarCarrito: () -> Unit,
    onProductClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onProductClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ProductImage(product.imagen,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(MaterialTheme.shapes.medium))
            Spacer(modifier = Modifier.height(12.dp))
            ProductInformation(productName = product.nombre, productPrice = product.precio, modifier = Modifier.fillMaxWidth())
            Text(
                text = if (product.stock > 0) "Disponibles: ${product.stock}" else "Agotado",
                style = MaterialTheme.typography.bodySmall,
                color = if (product.stock > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Envío gratis", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                Button(
                    onClick = onAgregarCarrito,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Añadir", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun SearchBarField(
    query: String,
    onQueryChange: (String) -> Unit,
    maxLength: Int=35,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = {nuevoTexto->
            if(nuevoTexto.length<=maxLength){
                onQueryChange(nuevoTexto)
            }
        },
        modifier = modifier,
        placeholder = { Text("Buscar producto...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar"
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar búsqueda"
                    )
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
fun PriceFilterRow(
    selectedFilter: PrecioFiltro,
    onFilterSelected: (PrecioFiltro) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(PrecioFiltro.entries) { filtro ->
            val isSelected = selectedFilter == filtro
            FilterChip(
                selected = isSelected,
                onClick = {
                    onFilterSelected(if (isSelected) PrecioFiltro.TODOS else filtro)
                },
                label = { Text(filtro.label) },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null
            )
        }
    }
}