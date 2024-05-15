package top.btswork.liteoss.common

object Constant {

  private const val TT = """#==== LiteOSS =================================================================="""
  private const val HR = """#==============================================================================="""

  private const val SEG_HEAD = TT + """
# A cURL and photoprism/immich friendly object storage service
"""

  private const val SEG_FOOT = """
# By Alceatraz Warprays with Ktor and GraalVM
""" + HR

  private const val TEXT_CLI = """
>> STARTUP SETTING - Use ENV then System Property then Default
OSS_SERVER_HOST/server.host = 0.0.0.0 ------------ Ktor bind config
OSS_SERVER_PORT/server.port = 80 ----------------- Ktor bind config
OSS_SCHEMA_BLOB/schema.blob = schema/blob -------- Path for data file
OSS_SCHEMA_DATA/schema.data = schema/data -------- Path for future usage
OSS_SCHEMA_LINK/schema.link = schema/link -------- Path for symbol links
OSS_SCHEMA_TEMP/schema.temp = schema/temp -------- Path for uploading file
>> WARNING: Link file use relative path, Which means:
1. Schema path can't change anymore OR ALL LINK'S TARGET MISSING
2. Container mount must as same as the host, For example:
/foo/bar/schema/blob:/photoprism/originals/liteoss/blob
/foo/bar/schema/link:/photoprism/originals/liteoss/link
"""

  private const val TEXT_API = """
>> ADMIN API
GET  /api/blob/size --------------- Calculate total blob size
GET  /api/blob/list --------------- List all blob name and size, Very Long !!!
GET  /api/link/size --------------- List all blob name and target, Very Long !!!
POST /api/blob/list --------------- Delete all link which target file not exist
"""

  private const val TEXT_OSS = """
# FILE MANAGE API
PUT     /oss/blob --------------------- Upload file (-d binary, Req body)
DELETE  /oss/blob/{hash} -------------- Delete file
HEAD    /oss/blob/{hash} -------------- Check is file exists
GET     /oss/blob/{hash} -------------- Download file content
"""

  private const val TEXT_OSS_BLOB = """
>> BLOB MANAGE API
PUT     /oss/blob --------------------- Upload file (-d binary, Req body)
DELETE  /oss/blob/{hash} -------------- Delete file
HEAD    /oss/blob/{hash} -------------- Check is file exists
GET     /oss/blob/{hash} -------------- Download file content
"""

  private const val TEXT_OSS_LINK = """
>> LINK MANAGE API
PUT    /oss/link/{name} -------------- Create link to blob (-d hash, Req body)
DELETE /oss/link/{hash} -------------- Delete link
HEAD   /oss/link/{hash} -------------- Check is link exist
GET    /oss/link/{hash} -------------- Get target file name
"""

  private const val TEXT_OSS_DATA = """
>> DATA MANAGE API
PUT     /oss/data/{name...} ---------- Upload file just override
POST    /oss/data/{name...} ---------- Upload file if not exist 
DELETE  /oss/data/{name...} ---------- Delete file
HEAD    /oss/data/{name...} ---------- Check is file exists
GET     /oss/data/{name...} ---------- Download file content
"""

  const val HELP_ALL = SEG_HEAD + HR + TEXT_CLI + HR + TEXT_API + HR + TEXT_OSS + HR + TEXT_OSS_BLOB + HR + TEXT_OSS_LINK + HR + TEXT_OSS_DATA + HR + SEG_FOOT
  const val HELP_API = HR + TEXT_API + HR
  const val HELP_OSS = HR + TEXT_OSS + HR + TEXT_OSS_BLOB + HR + TEXT_OSS_DATA + HR + TEXT_OSS_LINK + HR
  const val HELP_BLOB = HR + TEXT_OSS_BLOB + HR
  const val HELP_LINK = HR + TEXT_OSS_LINK + HR
  const val HELP_DATA = HR + TEXT_OSS_DATA + HR

}
