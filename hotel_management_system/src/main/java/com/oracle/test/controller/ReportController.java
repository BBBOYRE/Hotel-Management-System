package com.oracle.test.controller;

import com.oracle.test.common.Result;
import com.oracle.test.mapper.CheckInRecordMapper;
import com.oracle.test.mapper.HotelOrderMapper;
import com.oracle.test.mapper.RoomMapper;
import com.oracle.test.service.HotelOrderService;
import com.oracle.test.util.ExcelExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired private HotelOrderService orderService;
    @Autowired private HotelOrderMapper orderMapper;
    @Autowired private CheckInRecordMapper checkMapper;
    @Autowired private RoomMapper roomMapper;

    /** 看板首屏汇总 */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalRooms", roomMapper.count(null, null, null, null));
        data.put("inHouseCount", checkMapper.countInHouse());

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Date start = c.getTime();
        c.add(Calendar.DAY_OF_MONTH, 1);
        Date end = c.getTime();
        data.put("todayOrderCount", orderMapper.count(null, null, start, end));

        c.setTime(new Date());
        c.add(Calendar.DAY_OF_MONTH, -29);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Date m30 = c.getTime();
        Date now = new Date();
        data.put("daily30", orderService.dailyRevenue(m30, now));
        data.put("roomTypeRevenue", orderService.roomTypeRevenue(m30, now));
        return Result.ok(data);
    }

    @GetMapping("/daily")
    public Result<List<Map<String, Object>>> daily(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                   @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return Result.ok(orderService.dailyRevenue(startDate, endDate));
    }

    @GetMapping("/by-type")
    public Result<List<Map<String, Object>>> byType(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return Result.ok(orderService.roomTypeRevenue(startDate, endDate));
    }

    @GetMapping("/daily/export")
    public void exportDaily(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                            HttpServletResponse response) throws IOException {
        List<Map<String, Object>> rows = orderService.dailyRevenue(startDate, endDate);
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("day",        "日期");
        headers.put("orderCount", "订单数");
        headers.put("amount",     "营收");
        ExcelExportUtil.export(response, "每日营收_" + System.currentTimeMillis(), headers, rows);
    }
}
