# GalleryDialog
[![Jitpack Link](https://jitpack.io/v/MarcoSanz12/GalleryDialog.svg)](https://jitpack.io/#MarcoSanz12/GalleryDialog)

GalleryDialog is a library created to display **images** and **360 panoramic images** in a comfortable and simple way. It allows adding explanatory text for the photo in a footer that appears and hides dynamically.

*Currently, it only works with image loading via URL using [Glide](https://github.com/bumptech/glide). In the future, I would like to add the option to pass created drawables or bitmaps.*

Standard images use the [TouchImageView](https://github.com/MikeOrtiz/TouchImageView) library, which allows zooming through gestures.

360 images use the [PanoramaGL](https://github.com/hannesa2/panoramaGL) library, which allows zooming through gestures and activating sensor rotation.

## Demonstration
**GalleryDialog** ->

![GalleryDialog demo](https://github.com/MarcoSanz12/GalleryDialog/assets/100943352/6b38c636-e66d-4dc6-bf9c-d2cdfe0a0c2e)

**GalleryDialog360** ->

![GalleryDialog360 demo](https://github.com/MarcoSanz12/GalleryDialog/assets/100943352/87c7e389-6a03-4a55-a56d-134e85dc3167)

*(Both galleries support screen rotation using the rotation button)*

## How to use
Both classes are [DialogFragments](https://developer.android.com/guide/fragments/dialogs), with a static function *newInstance()* to facilitate their creation.

I recommend creating an extended function to show the dialog when clicking on an ImageView.
```kotlin
// GalleryDialog
    private fun ImageView.setGalleryDialogOnClick(position: Int) = setOnClickListener {
        GalleryDialog.newInstance(
            images,
            position,
            GalleryDialogOptions(
                fileProviderAuthorities = "com.marcosanz.app",
                errorDrawable = R.drawable.ic_launcher_foreground,
                rotation = false
            )
        ).show(supportFragmentManager, "gallery_dialog")
    }
```

```kotlin
// Gallery360Dialog
private fun ImageView.setGallery360DialogOnClick(position: Int) = setOnClickListener {
        Gallery360Dialog.newInstance(
            images360[position],
            Gallery360DialogOptions(
                rotation = true,
                sensorialRotation = true
            )
        ).show(supportFragmentManager, "gallery_dialog")
    }
```

With the DialogOptions classes, you can customize some minor settings, such as the texts displayed, allow rotation, or enable sensor rotation.

Check the sample app to see all these features implemented.

### Additional Configuration
- To **share images**, you must declare a [FileProvider](https://developer.android.com/reference/androidx/core/content/FileProvider) and pass the FileProviderAuthorities through GalleryDialogOptions.
- If the Activity has the *android:configChanges="layoutDirection"* field set in the Manifest, screen rotation will not work correctly.

# Installation
Add the Jitpack repository to your build.gradle file
```gradle
	dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
 ```
Add the dependency in your module
```gradle
dependencies {
	        implementation 'com.github.MarcoSanz12:GalleryDialog:{ LATEST_VERSION }'
	}
 ```

# License
GalleryDialog is available under the MIT license. Read the [LICENSE.txt](LICENSE.txt) file for more information.
