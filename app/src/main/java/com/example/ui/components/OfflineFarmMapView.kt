package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet4Bar
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.CooperativeEntity
import com.example.data.db.entities.FarmerEntity

data class OfflineFarmMarker(
    val id: String,
    val farmerName: String,
    val coopName: String,
    val county: String,
    val district: String,
    val latitude: Double,
    val longitude: Double,
    val tileCode: String,
    val cropType: String, // CASSAVA, YAM, SWEET_POTATO
    val acreageHa: Double,
    val roadCondition: String, // e.g. "Feeder Road (4x4)", "Paved Corridor"
    val distanceKm: Double,
    val relativeX: Float, // 0.0 to 1.0 on tile grid canvas
    val relativeY: Float  // 0.0 to 1.0 on tile grid canvas
)

object OfflineMapDataProvider {
    val sampleMarkers = listOf(
        OfflineFarmMarker(
            id = "FARM_001",
            farmerName = "Flomo Kpelle",
            coopName = "Ganta Farmers Co-op",
            county = "Nimba",
            district = "Bain-Garr",
            latitude = 7.234,
            longitude = -8.981,
            tileCode = "Z10-X512-Y408",
            cropType = "CASSAVA",
            acreageHa = 4.5,
            roadCondition = "Primary Paved Corridor",
            distanceKm = 3.2,
            relativeX = 0.35f,
            relativeY = 0.42f
        ),
        OfflineFarmMarker(
            id = "FARM_002",
            farmerName = "Martha Sahn",
            coopName = "Ganta Farmers Co-op",
            county = "Nimba",
            district = "Ganta Rural",
            latitude = 7.251,
            longitude = -8.965,
            tileCode = "Z10-X512-Y409",
            cropType = "CASSAVA",
            acreageHa = 6.2,
            roadCondition = "Unpaved Feeder (4x4 Only)",
            distanceKm = 8.7,
            relativeX = 0.58f,
            relativeY = 0.28f
        ),
        OfflineFarmMarker(
            id = "FARM_003",
            farmerName = "Kollie Zayzay",
            coopName = "Voinjama Produce Guild",
            county = "Lofa",
            district = "Voinjama Central",
            latitude = 8.421,
            longitude = -9.748,
            tileCode = "Z10-X508-Y392",
            cropType = "CASSAVA",
            acreageHa = 8.0,
            roadCondition = "Dirt Feeder Track",
            distanceKm = 14.1,
            relativeX = 0.22f,
            relativeY = 0.72f
        ),
        OfflineFarmMarker(
            id = "FARM_004",
            farmerName = "Samuel Quiah",
            coopName = "Zorzor Starch Union",
            county = "Lofa",
            district = "Zorzor District",
            latitude = 7.781,
            longitude = -9.429,
            tileCode = "Z10-X509-Y398",
            cropType = "YAM",
            acreageHa = 3.8,
            roadCondition = "Feeder Bridge Open",
            distanceKm = 11.5,
            relativeX = 0.78f,
            relativeY = 0.61f
        ),
        OfflineFarmMarker(
            id = "FARM_005",
            farmerName = "Emanuel Dahnsaw",
            coopName = "Sanniquellie Tuber Guild",
            county = "Nimba",
            district = "Sanniquellie Mah",
            latitude = 7.362,
            longitude = -8.712,
            tileCode = "Z10-X514-Y405",
            cropType = "SWEET_POTATO",
            acreageHa = 5.0,
            roadCondition = "Gravel Road Good",
            distanceKm = 18.3,
            relativeX = 0.85f,
            relativeY = 0.20f
        )
    )
}

@Composable
fun OfflineFarmMapView(
    farmers: List<FarmerEntity>,
    cooperatives: List<CooperativeEntity>,
    onSelectFarmer: (FarmerEntity) -> Unit = {}
) {
    var selectedCounty by remember { mutableStateOf("ALL") } // ALL, NIMBA, LOFA
    var selectedLayer by remember { mutableStateOf("TOPOGRAPHIC") } // TOPOGRAPHIC, SATELLITE, FEEDER_ROADS
    var zoomLevel by remember { mutableFloatStateOf(10f) } // 8f to 12f
    var selectedMarker by remember { mutableStateOf<OfflineFarmMarker?>(OfflineMapDataProvider.sampleMarkers[0]) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredMarkers = remember(selectedCounty, searchQuery) {
        OfflineMapDataProvider.sampleMarkers.filter { marker ->
            val matchesCounty = when (selectedCounty) {
                "NIMBA" -> marker.county.equals("Nimba", ignoreCase = true)
                "LOFA" -> marker.county.equals("Lofa", ignoreCase = true)
                else -> true
            }
            val matchesSearch = marker.farmerName.contains(searchQuery, ignoreCase = true) ||
                    marker.coopName.contains(searchQuery, ignoreCase = true) ||
                    marker.district.contains(searchQuery, ignoreCase = true)
            matchesCounty && matchesSearch
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("offline_farm_map_container")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Map Top Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = CircleShape,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = Color(0xFF0B3D2E),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Offline Tile Field Map",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0B3D2E)
                        )
                        Text(
                            text = "Locate Remote Farms Without Internet",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                Badge(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.DownloadDone, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tiles 100% Cached (28 MB)", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar & County Filter Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search farm pin or district...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("offline_map_search_input")
                )

                // County Filter Toggle Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        color = if (selectedCounty == "ALL") Color(0xFF0B3D2E) else Color(0xFFF1F8E9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { selectedCounty = "ALL" }
                            .testTag("map_filter_all")
                    ) {
                        Text(
                            text = "All",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedCounty == "ALL") Color.White else Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }

                    Surface(
                        color = if (selectedCounty == "NIMBA") Color(0xFF0B3D2E) else Color(0xFFF1F8E9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { selectedCounty = "NIMBA" }
                            .testTag("map_filter_nimba")
                    ) {
                        Text(
                            text = "Nimba",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedCounty == "NIMBA") Color.White else Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }

                    Surface(
                        color = if (selectedCounty == "LOFA") Color(0xFF0B3D2E) else Color(0xFFF1F8E9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { selectedCounty = "LOFA" }
                            .testTag("map_filter_lofa")
                    ) {
                        Text(
                            text = "Lofa",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedCounty == "LOFA") Color.White else Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Map Display Canvas with Tile Grid & Markers
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Color(0xFFE0F2F1), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF80CBC4), RoundedCornerShape(16.dp))
            ) {
                // Canvas Tile Grid Renderer
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(filteredMarkers) {
                            detectTapGestures { tapOffset ->
                                val width = size.width
                                val height = size.height

                                // Find nearest marker to tap
                                val hitMarker = filteredMarkers.minByOrNull { marker ->
                                    val mx = marker.relativeX * width
                                    val my = marker.relativeY * height
                                    val dx = mx - tapOffset.x
                                    val dy = my - tapOffset.y
                                    dx * dx + dy * dy
                                }

                                if (hitMarker != null) {
                                    val mx = hitMarker.relativeX * width
                                    val my = hitMarker.relativeY * height
                                    val distSq = (mx - tapOffset.x) * (mx - tapOffset.x) + (my - tapOffset.y) * (my - tapOffset.y)
                                    if (distSq < 2500f) { // Within 50px radius
                                        selectedMarker = hitMarker
                                    }
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    // 1. Draw Offline Map Background Canvas Color based on layer
                    val bgColor = when (selectedLayer) {
                        "SATELLITE" -> Color(0xFF2E3B32)
                        "FEEDER_ROADS" -> Color(0xFFF1F8E9)
                        else -> Color(0xFFE8F5E9)
                    }
                    drawRect(color = bgColor, size = size)

                    // 2. Draw Offline Vector Map Tile Grid Lines
                    val gridCols = 4
                    val gridRows = 4
                    val colWidth = width / gridCols
                    val rowHeight = height / gridRows

                    for (i in 1 until gridCols) {
                        drawLine(
                            color = Color(0x33000000),
                            start = Offset(i * colWidth, 0f),
                            end = Offset(i * colWidth, height),
                            strokeWidth = 1f
                        )
                    }

                    for (j in 1 until gridRows) {
                        drawLine(
                            color = Color(0x33000000),
                            start = Offset(0f, j * rowHeight),
                            end = Offset(width, j * rowHeight),
                            strokeWidth = 1f
                        )
                    }

                    // 3. Draw County Boundaries & River Contours (St. Paul & Mani Rivers)
                    val riverPath = Path().apply {
                        moveTo(0f, height * 0.3f)
                        cubicTo(
                            width * 0.25f, height * 0.4f,
                            width * 0.6f, height * 0.15f,
                            width, height * 0.5f
                        )
                    }
                    drawPath(
                        path = riverPath,
                        color = Color(0xFF80DEEA),
                        style = Stroke(width = 6f)
                    )

                    // Secondary Feeder Road Lines
                    val roadPath = Path().apply {
                        moveTo(width * 0.1f, 0f)
                        lineTo(width * 0.4f, height * 0.5f)
                        lineTo(width * 0.85f, height)
                    }
                    drawLine(
                        color = if (selectedLayer == "SATELLITE") Color(0xFFFFD54F) else Color(0xFF81C784),
                        start = Offset(width * 0.1f, 0f),
                        end = Offset(width * 0.4f, height * 0.5f),
                        strokeWidth = 4f
                    )
                    drawLine(
                        color = if (selectedLayer == "SATELLITE") Color(0xFFFFD54F) else Color(0xFF81C784),
                        start = Offset(width * 0.4f, height * 0.5f),
                        end = Offset(width * 0.85f, height),
                        strokeWidth = 4f
                    )

                    // 4. Draw Tile Codes on Canvas Grid (Offline Tile Verification)
                    val tileCodes = listOf(
                        "Z10-X508-Y392", "Z10-X509-Y398", "Z10-X512-Y408", "Z10-X514-Y405"
                    )
                    var tileIdx = 0
                    for (r in 0 until gridRows step 2) {
                        for (c in 0 until gridCols step 2) {
                            if (tileIdx < tileCodes.size) {
                                val tileText = tileCodes[tileIdx]
                                // Draw subtle tile marker
                                drawCircle(
                                    color = Color(0x22000000),
                                    radius = 4f,
                                    center = Offset(c * colWidth + 20f, r * rowHeight + 20f)
                                )
                                tileIdx++
                            }
                        }
                    }

                    // 5. Draw Farm Pin Markers
                    filteredMarkers.forEach { marker ->
                        val mx = marker.relativeX * width
                        val my = marker.relativeY * height
                        val isSelected = selectedMarker?.id == marker.id

                        val pinColor = when (marker.cropType) {
                            "CASSAVA" -> Color(0xFF2E7D32)
                            "YAM" -> Color(0xFFD84315)
                            else -> Color(0xFFF57F17)
                        }

                        // Selected Halo Ring
                        if (isSelected) {
                            drawCircle(
                                color = Color(0x66FFD54F),
                                radius = 22f,
                                center = Offset(mx, my)
                            )
                        }

                        // Pin Base Shadow
                        drawCircle(
                            color = Color(0x44000000),
                            radius = 12f,
                            center = Offset(mx, my + 4f)
                        )

                        // Main Pin Circle
                        drawCircle(
                            color = if (isSelected) Color(0xFFFFD54F) else pinColor,
                            radius = 12f,
                            center = Offset(mx, my)
                        )

                        // Inner Pin Dot
                        drawCircle(
                            color = Color.White,
                            radius = 4f,
                            center = Offset(mx, my)
                        )
                    }
                }

                // Map Overlay Toolbar Controls (Zoom + / - & Layer Switcher)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 2.dp,
                        modifier = Modifier.clickable {
                            selectedLayer = when (selectedLayer) {
                                "TOPOGRAPHIC" -> "SATELLITE"
                                "SATELLITE" -> "FEEDER_ROADS"
                                else -> "TOPOGRAPHIC"
                            }
                        }
                    ) {
                        Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Layers, contentDescription = "Layers", tint = Color(0xFF0B3D2E), modifier = Modifier.size(18.dp))
                        }
                    }

                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 2.dp,
                        modifier = Modifier.clickable {
                            if (zoomLevel < 12f) zoomLevel += 1f
                        }
                    ) {
                        Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color(0xFF0B3D2E), modifier = Modifier.size(18.dp))
                        }
                    }

                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 2.dp,
                        modifier = Modifier.clickable {
                            if (zoomLevel > 8f) zoomLevel -= 1f
                        }
                    ) {
                        Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color(0xFF0B3D2E), modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Bottom Left Tile Info Tag
                Surface(
                    color = Color(0xCC000000),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Layer: $selectedLayer • Zoom: Z${zoomLevel.toInt()} • Offline GPS Lock",
                        fontSize = 9.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selected Farm Pin Details Inspection Card
            selectedMarker?.let { farm ->
                Surface(
                    color = Color(0xFFF6F9F7),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFF0B3D2E),
                                    shape = CircleShape,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = farm.farmerName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0B3D2E)
                                    )
                                    Text(
                                        text = "${farm.coopName} • ${farm.district}, ${farm.county}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Badge(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)) {
                                Text("${farm.distanceKm} km away", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // GPS Coordinates & Road Access Details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("GPS Coordinates:", fontSize = 10.sp, color = Color.Gray)
                                Text("${farm.latitude}° N, ${farm.longitude}° W", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B3D2E))
                            }

                            Column {
                                Text("Offline Tile Code:", fontSize = 10.sp, color = Color.Gray)
                                Text(farm.tileCode, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B3D2E))
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Parcel Area:", fontSize = 10.sp, color = Color.Gray)
                                Text("${farm.acreageHa} Hectares", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null, tint = Color(0xFFD84315), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Access Road: ${farm.roadCondition}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFD84315)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val matched = farmers.find { it.fullName.contains(farm.farmerName, ignoreCase = true) }
                                    if (matched != null) onSelectFarmer(matched)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B3D2E)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("inspect_farmer_profile_button")
                            ) {
                                Text("Farmer Record", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("navigate_offline_route_button")
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Compass Route", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
