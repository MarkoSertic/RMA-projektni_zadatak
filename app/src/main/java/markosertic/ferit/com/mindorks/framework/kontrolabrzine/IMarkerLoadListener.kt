package markosertic.ferit.com.mindorks.framework.kontrolabrzine


interface IMarkerLoadListener {
    fun onLoadMarkerSuccess(CameraModelList: List<CameraModel>)
    fun onLoadMarkerFailed(message:String?)
}