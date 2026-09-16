var dataGiornata = new Array()
// Immettere le date nel formato inglese: Mese Giorno Anno
// Gennaio = January
// Febbraio = February
// Marzo = March
// Aprile = April
// Maggio = May
// Giugno = June
// Luglio = July
// Agosto = August
// Settembre = September
// Ottobre = October
// Novembre = November
// Dicembre = December
dataGiornata[1] = "september 19 2020 18:00"
dataGiornata[2] = "september 26 2020 15:00"
dataGiornata[3] = "october 02 2020 20:45"
dataGiornata[4] = "october 17 2020 15:00"
dataGiornata[5] = "october 24 2020 15:00"
dataGiornata[6] = "october 31 2020 15:00"
dataGiornata[7] = "november 07 2020 15:00"
dataGiornata[8] = "november 21 2020 15:00"
dataGiornata[9] = "november 28 2020 15:00"
dataGiornata[10] = "december 05 2020 15:00"
dataGiornata[11] = "december 12 2020 15:00"
dataGiornata[12] = "december 15 2020 21:00"
dataGiornata[13] = "december 19 2020 15:00"
dataGiornata[14] = "december 22 2020 21:00"
dataGiornata[15] = "january 02 2021 15:00"
dataGiornata[16] = "january 05 2021 21:00"
dataGiornata[17] = "january 09 2021 15:00"
dataGiornata[18] = "january 16 2021 15:00"
dataGiornata[19] = "january 23 2021 15:00"
dataGiornata[20] = "january 30 2021 15:00"
dataGiornata[21] = "february 06 2021 15:00"
dataGiornata[22] = "february 13 2021 15:00"
dataGiornata[23] = "february 20 2021 15:00"
dataGiornata[24] = "february 27 2021 15:00"
dataGiornata[25] = "march 02 2021 21:00"
dataGiornata[26] = "march 06 2021 15:00"
dataGiornata[27] = "march 13 2021 15:00"
dataGiornata[28] = "march 20 2021 15:00"
dataGiornata[29] = "april 02 2021 12:00"
dataGiornata[30] = "april 10 2021 15:00"
dataGiornata[31] = "april 17 2021 15:00"
dataGiornata[32] = "april 20 2021 21:00"
dataGiornata[33] = "april 24 2021 15:00"
dataGiornata[34] = "may 01 2021 15:00"
dataGiornata[35] = "may 08 2021 15:00"
dataGiornata[36] = "may 11 2021 21:00"
dataGiornata[37] = "may 15 2021 15:00"
dataGiornata[38] = "may 22 2021 15:00"

function initArray() {  
	this.length = initArray.arguments.length
    for (var i = 0; i < this.length; i++)
    this[i+1] = initArray.arguments[i]
}
var DOWArray = new initArray("Dom","Lun","Mar","Mer","Gio","Ven","Sab")
var MOYArray = new initArray("Gen","Feb","Mar","Apr","Mag","Giu","Lug","Ago","Set","Ott","Nov","Dic")
var Year
//for (t = 1; t < dataGiornata.length-1 ;t++ ) {
for (t = 1; t < dataGiornata.length ;t++ ) {
	data = new Date(dataGiornata[t])
	Year = data.getYear()
	if (Year < 2000)
		Year = Year + 1900
	dataGiornata[t] = DOWArray[(data.getDay()+1)] + " " + data.getDate() + " " + MOYArray[(data.getMonth()+1)] + " " + Year
}