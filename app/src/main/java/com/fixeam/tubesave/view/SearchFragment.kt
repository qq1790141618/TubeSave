package com.fixeam.tubesave.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fixeam.tubesave.network.ApiUtils
import com.fixeam.tubesave.R
import com.fixeam.tubesave.controller.openYouTubeVideo
import com.fixeam.tubesave.databinding.FragmentSearchBinding
import com.fixeam.tubesave.databinding.SearchItemBinding
import com.fixeam.tubesave.model.TuDown
import com.fixeam.tubesave.model.YoutubeSearch
import com.fixeam.tubesave.model.YoutubeSearch.SearchResult
import com.fixeam.tubesave.utils.DateTimeUtils
import com.fixeam.tubesave.utils.LoadingShow
import com.fixeam.tubesave.utils.ScreenUtils
import com.fixeam.tubesave.utils.SimpleDialog
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import kotlin.math.floor

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private var adapter: SearchItemsAdapter? = null
    private var total: Int = 0
    private var keyword: String? = null
    private var nextPageToken: String? = null
    private val items = mutableListOf<SearchResult>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initList()
        binding.searchInputLayout.searchButton.setOnClickListener { search() }
        binding.searchInputLayout.inputField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                search()
                return@setOnEditorActionListener true
            }
            false
        }

        read()
    }

    override fun onResume() {
        super.onResume()
        if (items.isNotEmpty()) switchView(0)
    }

    private fun switchView(number: Number) {
        binding.recyclerView.visibility = View.GONE
        binding.searchLayout.root.visibility = View.GONE
        binding.emptyLayout.root.visibility = View.GONE
        when(number) {
            0 -> binding.recyclerView.visibility = View.VISIBLE
            1 -> binding.searchLayout.root.visibility = View.VISIBLE
            2 -> binding.emptyLayout.root.visibility = View.VISIBLE
        }
    }

    private fun initList() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.setItemViewCacheSize(3)
        binding.recyclerView.setHasFixedSize(true)

        adapter = SearchItemsAdapter()
        binding.recyclerView.adapter = adapter

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                layoutManager.findFirstVisibleItemPosition()
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItemPosition > items.size - 3 && total > items.size && getString(R.string.app_type) != "dev") {
                    val size = items.size
                    requestSearch {
                        saveConfig()
                        saveList()
                        adapter?.notifyItemInserted(size)
                    }
                }
            }
        })
    }

    private fun saveConfig() {
        val context = requireContext()
        val sharedPreferences = context.getSharedPreferences("search", MODE_PRIVATE)
        sharedPreferences.edit().putInt("total", total).apply()
        sharedPreferences.edit().putString("keyword", keyword).apply()
        sharedPreferences.edit().putString("nextPageToken", nextPageToken).apply()
    }

    private fun saveList() {
        val context = requireContext()
        val json = Gson().toJson(items)
        val file = File(context.filesDir, "search_list.json")
        file.writeText(json)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun read() {
        val context = requireContext()
        val file = File(context.filesDir, "search_list.json")
        if (file.exists()) {
            val jsonFromFile = file.readText()
            val objectList = Gson().fromJson(jsonFromFile, Array<SearchResult>::class.java).toList()
            items.clear()
            items.addAll(objectList)
            if (objectList.isNotEmpty()) switchView(0)
        }
        adapter?.notifyDataSetChanged()
        val sharedPreferences = context.getSharedPreferences("search", MODE_PRIVATE)
        total = sharedPreferences.getInt("total", total)
        keyword = sharedPreferences.getString("keyword", keyword)
        nextPageToken = sharedPreferences.getString("nextPageToken", nextPageToken)
        keyword?.let { binding.searchInputLayout.inputField.text = Editable.Factory.getInstance().newEditable(it) }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun search() {
        // 关闭键盘
        binding.searchInputLayout.inputField.clearFocus()
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.searchInputLayout.inputField.windowToken, 0)
        // 获取关键词
        keyword = binding.searchInputLayout.inputField.text.toString()
        if (keyword.isNullOrBlank()) {
            SimpleDialog(requireContext(), "搜索内容不能为空!")
            return
        }
        // 清除状态
        items.clear()
        nextPageToken = null
        total = 0
        // 判断字段类型
        if (keyword!!.startsWith("https://") || keyword!!.startsWith("http://")) {
            requestVideoData(keyword!!)
        } else if (keyword!!.length == 11 && isValidString(keyword!!)) {
            requestVideoData("https://www.youtube.com/watch?v=$keyword")
        } else {
            // 跳转加载
            switchView(0)
            LoadingShow(requireContext(), true, "搜索中...")
            requestSearch {
                LoadingShow(requireContext(), false)
                saveConfig()
                saveList()
                adapter?.notifyDataSetChanged()
                if (total == 0) {
                    switchView(2)
                }
            }
        }
    }

    private fun isValidString(input: String): Boolean {
        // 使用正则表达式判断字符串是否只包含字母、数字和下划线
        val regex = "^[a-zA-Z0-9_]+$".toRegex()
        return regex.matches(input)
    }

    private val apiKey = "AIzaSyCIQoxJ-rdCpIT45trpU75Kdb9ccVRJPIE"

    private fun requestSearch(call: () -> Unit) {
        val maxResults = if (getString(R.string.app_type) != "dev") 5 else 10
        var url = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=$maxResults&q=$keyword&type=video&key=$apiKey"
        nextPageToken?.let {
            url += "&pageToken=$it"
        }
        ApiUtils().service.search(url).enqueue(object: Callback<YoutubeSearch.SearchListResponse> {
            override fun onResponse(call: Call<YoutubeSearch.SearchListResponse>, response: Response<YoutubeSearch.SearchListResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    body.nextPageToken?.let { nextPageToken = it }
                    total = body.pageInfo.totalResults
                    if (body.items.isNotEmpty()) {
                        requestViews(body.items) {
                            items.addAll(body.items)
                            call()
                        }
                    } else {
                        call()
                    }
                } else {
                    call()
                }
            }
            override fun onFailure(call: Call<YoutubeSearch.SearchListResponse>, t: Throwable) {
                Log.e("ABLog", "Error: $t")
                call()
            }
        })
    }

    private fun requestViews(items: List<SearchResult>, call: () -> Unit) {
        val url = "https://www.googleapis.com/youtube/v3/videos?part=statistics&id=${ items.joinToString(",") { it.id.videoId } }&key=$apiKey"
        ApiUtils().service.getViews(url).enqueue(object: Callback<YoutubeSearch.VideoListResponse> {
            override fun onResponse(call: Call<YoutubeSearch.VideoListResponse>, response: Response<YoutubeSearch.VideoListResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    body.items.map { result ->
                        items.map { item ->
                            if (item.id.videoId == result.id) {
                                item.statistics = result.statistics
                            }
                        }
                    }
                }
                call()
            }
            override fun onFailure(call: Call<YoutubeSearch.VideoListResponse>, t: Throwable) {
                Log.e("ABLog", "Error: $t")
                call()
            }
        })
    }

    private fun requestVideoData(url: String) {
        val screenUtils = ScreenUtils(requireContext())
        val fail = "视频转码信息读取失败"
        LoadingShow(requireContext(), true, "视频读取中...")
        ApiUtils().service
            .getDowns("https://tubedown.cn/api/youtube", TuDown.TuDownRequestBody(url))
            .enqueue(object: Callback<TuDown.TuDownResponse> {
            override fun onResponse(call: Call<TuDown.TuDownResponse>, response: Response<TuDown.TuDownResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.message == "success") {
                        DownloadView(
                            binding.root,
                            requireActivity() as AppCompatActivity,
                            body.data
                        )
                    } else {
                        screenUtils.showToast(fail)
                    }
                } else {
                    screenUtils.showToast(fail)
                }
                LoadingShow(requireContext(), false)
            }
            override fun onFailure(call: Call<TuDown.TuDownResponse>, t: Throwable) {
                Log.e("ABLog", "Error: $t")
                screenUtils.showToast(fail)
                LoadingShow(requireContext(), false)
            }
        })
    }

    inner class SearchItemsAdapter : RecyclerView.Adapter<SearchItemHolder>() {
        override fun getItemCount(): Int {
            return items.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchItemHolder {
            val binding = SearchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SearchItemHolder(binding)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: SearchItemHolder, position: Int) {
            val item = items[position]
            Glide.with(requireContext())
                .load(item.snippet.thumbnails.high.url)
                .placeholder(R.drawable.image_holder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.binding.coverView)
            holder.binding.title.text = Html.fromHtml(item.snippet.title, Html.FROM_HTML_MODE_COMPACT)
            var profile = "${Html.fromHtml(item.snippet.channelTitle, Html.FROM_HTML_MODE_COMPACT)} · "
            item.statistics?.let {
                profile += "${calculateViews(it.viewCount)}次观看 · "
            }
            profile += DateTimeUtils.calculateTimeAgo(item.snippet.publishTime.replace("T", " ").replace("Z", ""))
            holder.binding.profile.text = profile
            holder.binding.download.setOnClickListener {
                requestVideoData("https://www.youtube.com/watch?v=${item.id.videoId}")
            }
            holder.itemView.setOnClickListener {
                openYouTubeVideo(requireContext(), item.id.videoId)
            }
        }

        private fun calculateViews(views: String): String {
            val viewCount = views.toLong()
            return if (viewCount > 100000000) {
                "${floor(viewCount.toDouble() / 10000000) / 10}亿"
            } else if (viewCount > 10000) {
                "${floor(viewCount.toDouble() / 1000) / 10}万"
            } else {
                views
            }
        }
    }

    class SearchItemHolder(val binding: SearchItemBinding) : RecyclerView.ViewHolder(binding.root)
}