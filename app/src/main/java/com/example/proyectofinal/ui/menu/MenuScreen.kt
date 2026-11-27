package com.example.proyectofinal.ui.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import coil.compose.AsyncImage
import com.example.proyectofinal.R
import com.example.proyectofinal.data.model.Producto
import com.example.proyectofinal.ui.TiendaViewModel
import com.example.proyectofinal.ui.components.RatingBar

enum class PrecioFiltro(val labelRes: Int) {
    TODOS(R.string.filtro_todos),
    MEJOR_CALIFICACION(R.string.filtro_mejor_calificados),
    MENOR_PRECIO(R.string.filtro_menor_precio),
    MAYOR_PRECIO(R.string.filtro_mayor_precio),
    MENOS_DE_500(R.string.filtro_menos_500)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    viewModel: TiendaViewModel,
    onCartClick: () -> Unit,
    onOpenMenu: () -> Unit,
    onProductClick: (Int) -> Unit,
    showMenuButton: Boolean = true,
    modifier: Modifier = Modifier,
    productoSeleccionadoId: Int? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedPriceFilter by rememberSaveable { mutableStateOf(PrecioFiltro.TODOS) }

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
            PrecioFiltro.MEJOR_CALIFICACION -> baseList.sortedByDescending { it.calificacion }
            else -> baseList
        }
    }

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val productColumns = when {
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> 3
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> 2
        else -> 1
    }

    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    // Esto permite que al hacer búsquedas o filtrar, se deslice automáticamente al primer ítem
    LaunchedEffect(selectedPriceFilter, searchQuery) {
        if (filteredProducts.isNotEmpty()) {
            if (productColumns == 1) {
                listState.animateScrollToItem(0)
            } else {
                gridState.animateScrollToItem(0)
            }
        }
    }

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let { mensaje ->
            snackbarHostState.showSnackbar(message = mensaje, duration = SnackbarDuration.Short)
            viewModel.limpiarMensaje()
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
        modifier = modifier,
        topBar = {
            MenuScreenTopAppBar(
                cartCount = uiState.shoppingCart.sumOf { it.cantidad },
                onMenuClick = onOpenMenu,
                onCartClick = onCartClick,
                showMenuButton = showMenuButton
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
                                stringResource(R.string.sin_productos)
                            else
                                stringResource(R.string.sin_productos_filtros),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (productColumns == 1) {
                    LazyColumn(
                        state = listState,
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
                                onProductClick = { onProductClick(producto.id) },
                                isSelected = productoSeleccionadoId == producto.id
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(productColumns),
                        state = gridState,
                        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = filteredProducts,
                            key = { it.id }
                        ) { producto ->
                            ProductGridItem(
                                product = producto,
                                onAgregarCarrito = { viewModel.agregarAlCarrito(producto) },
                                onProductClick = { onProductClick(producto.id) },
                                isSelected = productoSeleccionadoId == producto.id
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
    showMenuButton: Boolean = true,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            if (showMenuButton) {
                IconButton(onClick = onMenuClick) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = null)
                }
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
                        contentDescription = stringResource(R.string.carrito)
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
        Text(text = "\$%.2f".format(productPrice), style = MaterialTheme.typography.bodyLarge)
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
    onProductClick: () -> Unit,
    isSelected: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        onClick = onProductClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ProductImage(
                product.imagen,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(MaterialTheme.shapes.medium)
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProductInformation(
                productName = product.nombre,
                productPrice = product.precio,
                modifier = Modifier.fillMaxWidth()
            )

            if (product.numReviews > 0) {
                RatingBar(
                    calificacion = product.calificacion,
                    numReviews = product.numReviews,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Text(
                text = if (product.stock > 0) stringResource(R.string.disponibles_count, product.stock) else stringResource(R.string.agotado),
                style = MaterialTheme.typography.bodyMedium,
                color = if (product.stock > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.envio_gratis),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF2E7D32)
                )
                Button(
                    onClick = onAgregarCarrito,
                    enabled = product.stock > 0,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(stringResource(R.string.agregar_al_carrito), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun ProductGridItem(
    product: Producto,
    modifier: Modifier = Modifier,
    onAgregarCarrito: () -> Unit,
    onProductClick: () -> Unit,
    isSelected: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        onClick = onProductClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            AsyncImage(
                model = product.imagen,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.nombre,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "\$%.2f".format(product.precio),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            if (product.numReviews > 0) {
                RatingBar(
                    calificacion = product.calificacion,
                    numReviews = product.numReviews,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Text(
                text = if (product.stock > 0) stringResource(R.string.disponibles_count, product.stock) else stringResource(R.string.agotado),
                style = MaterialTheme.typography.bodySmall,
                color = if (product.stock > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = stringResource(R.string.envio_gratis),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(top = 8.dp)
            )

            Button(
                onClick = onAgregarCarrito,
                enabled = product.stock > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Text(stringResource(R.string.agregar_al_carrito), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun SearchBarField(
    query: String,
    onQueryChange: (String) -> Unit,
    maxLength: Int = 35,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = { nuevoTexto ->
            if (nuevoTexto.length <= maxLength) {
                onQueryChange(nuevoTexto)
            }
        },
        modifier = modifier,
        placeholder = { Text(stringResource(R.string.buscar_producto)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null
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
                label = { Text(stringResource(filtro.labelRes)) },
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