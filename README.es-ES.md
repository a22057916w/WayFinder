## Descripción general
WayFinder es una aplicación de navegación interior que indica al usuario la dirección y la ruta mediante huellas de AR. El método desarrollado se puede aplicar en cualquier edificio que tenga un plano, como un aeropuerto, centro comercial o estacionamiento.

<br>

* [mapbox](https://docs.mapbox.com/android/maps/examples/) - mapa interior
* [ARCore](https://developers.google.com/ar/develop/java/augmented-images) - reconocimiento de imágenes, seguimiento de movimiento e indicador de huellas

<br>

## Demo
<p align="center">
<img src="/.meta/demo1.gif" width="30%" height="30%">&nbsp;&nbsp;
<img src="/.meta/demo2.gif" width="30%" height="30%">&nbsp;&nbsp;
<img src="/.meta/demo3.gif" width="30%" height="30%"><br><br>
</p>

Se elige el destino en la página principal y se busca la imagen específica como punto de partida para comenzar la navegación. Posteriormente, la ruta se calculará y se mostrará como una serie de huellas de AR. Finalmente, se siguen las huellas hasta llegar al destino.

## Problemas
* La ruta o las huellas de AR pueden verse afectadas por la fuente de luz ambiental
* El plano no coincide exactamente con el edificio real

## Licencia
Disponible bajo la [MIT License](https://github.com/a22057916w/WayFinder/blob/master/LICENSE)
