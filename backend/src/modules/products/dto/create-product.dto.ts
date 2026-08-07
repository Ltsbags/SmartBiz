import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsNotEmpty, IsNumber, IsOptional, IsString, Min } from 'class-validator';

export class CreateProductDto {
  @ApiProperty({ example: 'Apple iPhone 15 Pro' })
  @IsString()
  @IsNotEmpty()
  name: string;

  @ApiPropertyOptional({ example: 'SKU-IPH-15P' })
  @IsOptional()
  @IsString()
  sku?: string;

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  categoryId?: string;

  @ApiPropertyOptional({ example: 'pcs' })
  @IsOptional()
  @IsString()
  unit?: string = 'pcs';

  @ApiProperty({ example: 85000.0 })
  @IsNumber()
  @Min(0)
  costPrice: number;

  @ApiProperty({ example: 99990.0 })
  @IsNumber()
  @Min(0)
  sellingPrice: number;

  @ApiProperty({ example: 10 })
  @IsNumber()
  @Min(0)
  stockQty: number;

  @ApiPropertyOptional({ example: 5 })
  @IsOptional()
  @IsNumber()
  @Min(0)
  minStockAlert?: number = 5;
}
